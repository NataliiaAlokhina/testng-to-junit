package org.example.recipes;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class ThrowableRecipeTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.parser(JavaParser.fromJavaVersion().classpath("java")).recipe(new ThrowableRecipe());
	}

	@Test
	void categoriesHavingJAssignmentArguments() {
		//language=java
		rewriteRun(java("""
				import org.testng.annotations.Test;
				 
				 import java.util.Collection;
				 import java.util.Collections;
				 
				 public class ExceptionTest {
				 
				     @org.testng.annotations.Test(expectedExceptions = NullPointerException.class)
				     public void transformException(){
				         System.out.println("Some code executed in test here");
				         Object object = null;
				 
				         // This block should be modified to catch throwable
				         try {
				             object.equals("string");
				         }
				         catch (NullPointerException e){
				            Collection pfValidator = Collections.emptyList();
				         }
				         // This block should be modified to catch throwable
				     }
				 }
				""", """
				                          
				 import org.junit.jupiter.api.Test;
				 
				 import static org.assertj.core.api.Assertions.*;
				 
				 public class ExceptionTest {
				 
				     @Test
				     public void transformException(){
				         System.out.println("Some code executed in test here");
				         Object object = null;
				 
				         // This block should be modified to catch throwable
				         var throwable = catchThrowable(()->{object.equals("string");});
				         assertThat(throwable).isInstanceOf(NullPointerException.class);
				         // This block should be modified to catch throwable
				     }
				 }
				"""));
	}

}