	package javax.annotation;
	
	import java.lang.annotation.Documented;
	import java.lang.annotation.Retention;
	import java.lang.annotation.RetentionPolicy;
	import javax.annotation.meta.TypeQualifier;
	import javax.annotation.meta.TypeQualifierValidator;
	import javax.annotation.meta.When;
	
	@Documented
	@Retention (RetentionPolicy.RUNTIME)
	@TypeQualifier
	public @interface NonNull {
		
		When when () default When.ALWAYS;
		
		class Checker implements TypeQualifierValidator<NonNull> {
			public When forConstantValue (NonNull qualifierArgument, Object value) {
				if (value == null) {
					return When.NEVER;
				}
				return When.ALWAYS;
			}
			
		}
		
	}