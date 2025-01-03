	/*
	 * Copyright (C) 2015 The Android Open Source Project
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *			http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	
	package androidx.annotation;
	
	import static java.lang.annotation.ElementType.CONSTRUCTOR;
	import static java.lang.annotation.ElementType.METHOD;
	import static java.lang.annotation.ElementType.PARAMETER;
	import static java.lang.annotation.ElementType.TYPE;
	import static java.lang.annotation.RetentionPolicy.CLASS;
	
	import java.lang.annotation.Documented;
	import java.lang.annotation.Retention;
	import java.lang.annotation.Target;
	
	/**
	 * Denotes that the annotated method or constructor should only be called on the UI thread.
	 * <pre><code>
	 *	&#64;UiThread
	 *	void setText(@NonNull String text) { ... }
	 * </code></pre>
	 *
	 * <p>If the annotated element is a class, then all methods in the class should be called
	 * on the UI thread. </p>
	 *
	 * <pre><code>
	 *	&#64;UiThread
	 *	public class Foo { ... }
	 * </code></pre>
	 *
	 * <p>When the class is annotated, but one of the methods has another threading annotation such as
	 * {@link WorkerThread}, the method annotation takes precedence. In the following example,
	 * <code>getUser()</code> should be called on a worker thread.</p>
	 *
	 * <pre><code>
	 *	&#64;UiThread
	 *	public class Foo {
	 *			&#64;WorkerThread
	 *			User getUser() { ... }
	 *	}
	 * </code></pre>
	 *
	 * <p>Multiple threading annotations can be combined. Following example illustrates that,
	 * <code>isEmpty()</code> can be called on a worker thread or the main thread.
	 * It's safe for <code>saveUser()</code> to invoke <code>isEmpty()</code>, whereas it's not safe
	 * for <code>isEmpty()</code> to invoke <code>saveUser()</code>.
	 * </p>
	 *
	 * <pre><code>
	 *	public class Foo {
	 *			&#64;WorkerThread
	 *			void saveUser(User user) { ... }
	 *
	 *			&#64;WorkerThread
	 *			&#64;UiThread
	 *			boolean isEmpty(String value) { ... }
	 *	}
	 * </code></pre>
	 *
	 * <p class="note"><b>Note:</b> Ordinarily, an app's UI thread is also the main
	 * thread. However, under special circumstances, an app's UI thread
	 * might not be its main thread; for more information, see
	 * <a href="/studio/write/annotations.html#thread-annotations">Thread
	 * annotations</a>.
	 *
	 * @see androidx.annotation.MainThread
	 */
	@Documented
	@Retention (CLASS)
	@Target ({METHOD, CONSTRUCTOR, TYPE, PARAMETER})
	public @interface UiThread {
	}