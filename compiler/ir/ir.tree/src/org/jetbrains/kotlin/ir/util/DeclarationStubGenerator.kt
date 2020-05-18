/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFieldImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrPropertyImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.declarations.lazy.*
import org.jetbrains.kotlin.ir.expressions.impl.IrErrorExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.load.java.descriptors.JavaCallableMemberDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.hasBackingField
import org.jetbrains.kotlin.types.KotlinType

class DeclarationStubGenerator(
    moduleDescriptor: ModuleDescriptor,
    val symbolTable: SymbolTable,
    val origin: IrDeclarationOrigin,
    val languageVersionSettings: LanguageVersionSettings,
    val deserializer: IrDeserializer? = null
) {

    private val lazyTable = symbolTable.lazyWrapper

    internal var unboundSymbolGeneration: Boolean
        get() = lazyTable.stubGenerator != null
        set(value) {
            lazyTable.stubGenerator = if (value) this else null
        }


    private val typeTranslator = TypeTranslator(lazyTable, languageVersionSettings, LazyScopedTypeParametersResolver(lazyTable), true)
    private val constantValueGenerator = ConstantValueGenerator(moduleDescriptor, lazyTable)

    init {
        typeTranslator.constantValueGenerator = constantValueGenerator
        constantValueGenerator.typeTranslator = typeTranslator
    }

    fun generateOrGetEmptyExternalPackageFragmentStub(descriptor: PackageFragmentDescriptor): IrExternalPackageFragment {
        val referenced = symbolTable.referenceExternalPackageFragment(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        return symbolTable.declareExternalPackageFragment(descriptor)
    }

    fun generateMemberStub(descriptor: DeclarationDescriptor): IrDeclaration =
        when (descriptor) {
            is ClassDescriptor ->
                if (DescriptorUtils.isEnumEntry(descriptor))
                    generateEnumEntryStub(descriptor)
                else
                    generateClassStub(descriptor)
            is ClassConstructorDescriptor ->
                generateConstructorStub(descriptor)
            is FunctionDescriptor ->
                generateFunctionStub(descriptor)
            is PropertyDescriptor ->
                generatePropertyStub(descriptor)
            else ->
                throw AssertionError("Unexpected member descriptor: $descriptor")
        }

    internal fun generatePropertyStub(
        descriptor: PropertyDescriptor,
        bindingContext: BindingContext? = null
    ): IrProperty = symbolTable.referenceProperty(descriptor) {
        deserializer?.findDeserializedDeclaration(descriptor) ?:
        IrLazyProperty(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor,
            this, typeTranslator, bindingContext
        )
    }

    fun generateFieldStub(descriptor: PropertyDescriptor, bindingContext: BindingContext? = null): IrField {
        val referenced = symbolTable.referenceField(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        val origin =
            if (descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
                IrDeclarationOrigin.FAKE_OVERRIDE
            else origin

        return symbolTable.declareField(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            origin,
            descriptor.original,
            descriptor.type.toIrType()
        ) {
            deserializer?.findDeserializedDeclaration(referenced) as? IrField ?:
            IrFieldImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, it, descriptor.type.toIrType())
        }.apply {
            initializer = descriptor.compileTimeInitializer?.let {
                IrExpressionBodyImpl(
                    constantValueGenerator.generateConstantValueAsExpression(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET, it
                    )
                )
            }
        }
    }

    fun generateFunctionStub(descriptor: FunctionDescriptor, createPropertyIfNeeded: Boolean = true): IrSimpleFunction {
        val referenced = symbolTable.referenceSimpleFunction(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        if (createPropertyIfNeeded && descriptor is PropertyGetterDescriptor) {
            return generatePropertyStub(descriptor.correspondingProperty).getter!!
        }
        if (createPropertyIfNeeded && descriptor is PropertySetterDescriptor) {
            return generatePropertyStub(descriptor.correspondingProperty).setter!!
        }

        val origin =
            if (descriptor.kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE)
                IrDeclarationOrigin.FAKE_OVERRIDE
            else if (origin == IrDeclarationOrigin.IR_EXTERNAL_DECLARATION_STUB && descriptor is JavaCallableMemberDescriptor)
                IrDeclarationOrigin.IR_EXTERNAL_JAVA_DECLARATION_STUB
            else
                origin
        return symbolTable.declareSimpleFunction(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            origin,
            descriptor.original
        ) {
            deserializer?.findDeserializedDeclaration(referenced) as? IrSimpleFunction ?:
            IrLazyFunction(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, it, this, typeTranslator)
        }
    }

    internal fun generateConstructorStub(descriptor: ClassConstructorDescriptor): IrConstructor {
        val referenced = symbolTable.referenceConstructor(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }

        return symbolTable.declareConstructor(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor.original
        ) {
            deserializer?.findDeserializedDeclaration(referenced) as? IrConstructor ?:
            IrLazyConstructor(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, it, this, typeTranslator)
        }
    }

    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    internal fun generateValueParameterStub(descriptor: ValueParameterDescriptor): IrValueParameter {
        return IrValueParameterImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
            descriptor, descriptor.type.toIrType(), descriptor.varargElementType?.toIrType()
        ).also { irValueParameter ->
            if (descriptor.declaresDefaultValue()) {
                irValueParameter.defaultValue =
                        IrExpressionBodyImpl(
                            IrErrorExpressionImpl(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET, descriptor.type.toIrType(),
                                "Stub expression for default value of ${descriptor.name}"
                            )
                        )
            }
        }
    }

    internal fun generateClassStub(descriptor: ClassDescriptor): IrClass {
        val referenceClass = symbolTable.referenceClass(descriptor)
        if (referenceClass.isBound) {
            return referenceClass.owner
        }
        return symbolTable.declareClass(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor
        ) {
            deserializer?.findDeserializedDeclaration(referenceClass) as? IrClass ?:
            IrLazyClass(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, it, this, typeTranslator)
        }
    }

    internal fun generateEnumEntryStub(descriptor: ClassDescriptor): IrEnumEntry {
        val referenced = symbolTable.referenceEnumEntry(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        return symbolTable.declareEnumEntry(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            deserializer?.findDeserializedDeclaration(referenced) as? IrEnumEntry ?:
            IrLazyEnumEntryImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, it, this, typeTranslator)
        }
    }

    internal fun generateOrGetTypeParameterStub(descriptor: TypeParameterDescriptor): IrTypeParameter {
        val referenced = symbolTable.referenceTypeParameter(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        return symbolTable.declareGlobalTypeParameter(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            deserializer?.findDeserializedDeclaration(referenced) as? IrTypeParameter ?:
            IrLazyTypeParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, this, typeTranslator
            )
        }
    }

    internal fun generateOrGetScopedTypeParameterStub(descriptor: TypeParameterDescriptor): IrTypeParameter {
        val referenced = symbolTable.referenceTypeParameter(descriptor)
        if (referenced.isBound) {
            return referenced.owner
        }
        return symbolTable.declareScopedTypeParameter(UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin, descriptor) {
            IrLazyTypeParameter(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, origin,
                it, this, typeTranslator
            )
        }
    }
}
