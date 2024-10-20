package javax.annotation.meta;

import java.lang.annotation.Annotation;
import javax.annotation.NonNull;

public interface TypeQualifierValidator<A extends Annotation> {
		@NonNull
		When forConstantValue(@NonNull A a, Object obj);
}
