	package upl.util.courutines;
	
	import java.lang.ref.WeakReference;
	import java.util.ArrayList;
	import java.util.List;
	
	public class CorRunRAII {
		
		protected List<WeakReference<? extends CorRun<?, ?>>> resources = new ArrayList<> ();
		
		public CorRunRAII add (CorRun<?, ?> resource) {
			
			if (resource == null)
				return this;
			
			resources.add (new WeakReference<> (resource));
			
			return this;
			
		}
		
		public CorRunRAII addAll (List<? extends CorRun<?, ?>> arrayList) {
			
			if (arrayList != null)
				for (CorRun<?, ?> corRun : arrayList)
					add (corRun);
				
			return this;
			
		}
		
		@Override
		protected void finalize () throws Throwable {
			
			super.finalize ();
			
			for (WeakReference<? extends CorRun<?, ?>> corRunWeakReference : resources) {
				
				CorRun<?, ?> corRun = corRunWeakReference.get ();
				
				if (corRun != null)
					corRun.stop ();
				
			}
			
		}
		
	}