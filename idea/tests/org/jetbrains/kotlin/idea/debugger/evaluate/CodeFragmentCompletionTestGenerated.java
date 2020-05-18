/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.debugger.evaluate;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/idea-completion/testData/basic/codeFragments")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class CodeFragmentCompletionTestGenerated extends AbstractCodeFragmentCompletionTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
    }

    public void testAllFilesPresentInCodeFragments() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/idea-completion/testData/basic/codeFragments"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("blockCodeFragment.kt")
    public void testBlockCodeFragment() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/blockCodeFragment.kt");
    }

    @TestMetadata("classHeader.kt")
    public void testClassHeader() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/classHeader.kt");
    }

    @TestMetadata("elementAt.kt")
    public void testElementAt() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/elementAt.kt");
    }

    @TestMetadata("elementAtFirstInBlock.kt")
    public void testElementAtFirstInBlock() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/elementAtFirstInBlock.kt");
    }

    @TestMetadata("localVal.kt")
    public void testLocalVal() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/localVal.kt");
    }

    @TestMetadata("localVariables.kt")
    public void testLocalVariables() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/localVariables.kt");
    }

    @TestMetadata("localVariablesOnReturn.kt")
    public void testLocalVariablesOnReturn() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/localVariablesOnReturn.kt");
    }

    @TestMetadata("noDuplicatesForSyntheticProperties.kt")
    public void testNoDuplicatesForSyntheticProperties() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/noDuplicatesForSyntheticProperties.kt");
    }

    @TestMetadata("privatesInSecondPressCompletion.kt")
    public void testPrivatesInSecondPressCompletion() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/privatesInSecondPressCompletion.kt");
    }

    @TestMetadata("topLevel.kt")
    public void testTopLevel() throws Exception {
        runTest("idea/idea-completion/testData/basic/codeFragments/topLevel.kt");
    }

    @TestMetadata("idea/idea-completion/testData/basic/codeFragments/runtimeType")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class RuntimeType extends AbstractCodeFragmentCompletionTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
        }

        public void testAllFilesPresentInRuntimeType() throws Exception {
            KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/idea-completion/testData/basic/codeFragments/runtimeType"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
        }

        @TestMetadata("castWithGenerics.kt")
        public void testCastWithGenerics() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/castWithGenerics.kt");
        }

        @TestMetadata("complexHierarchy.kt")
        public void testComplexHierarchy() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/complexHierarchy.kt");
        }

        @TestMetadata("extensionMethod.kt")
        public void testExtensionMethod() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/extensionMethod.kt");
        }

        @TestMetadata("notImportedExtension.kt")
        public void testNotImportedExtension() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/notImportedExtension.kt");
        }

        @TestMetadata("runtimeCast.kt")
        public void testRuntimeCast() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/runtimeCast.kt");
        }

        @TestMetadata("smartCompletion.kt")
        public void testSmartCompletion() throws Exception {
            runTest("idea/idea-completion/testData/basic/codeFragments/runtimeType/smartCompletion.kt");
        }
    }
}
