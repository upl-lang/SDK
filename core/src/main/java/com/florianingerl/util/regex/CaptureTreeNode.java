	package com.florianingerl.util.regex;
	
	import java.util.LinkedList;
	import java.util.List;
	import java.util.ListIterator;
	import java.util.Map;
	
	/**
	 * A node inside a {@link CaptureTree}.
	 *
	 * @author Florian Ingerl
	 * @see CaptureTree
	 * @see Matcher#captureTree()
	 */
	public class CaptureTreeNode {
		
		public int groupNumber;
		public String groupName;
		public Capture capture;
		public LinkedList<CaptureTreeNode> children = new LinkedList<CaptureTreeNode> ();
		public CaptureTreeNode parent;
		public boolean recursion;
		public boolean inLookaround;
		
		/**
		 * Returns the group number of the <a href="Pattern.html#cg">capturing
		 * group</a> that was captured
		 *
		 * @return The group number of the <a href="Pattern.html#cg">capturing
		 * group</a> that was captured
		 */
		public int getGroupNumber () {
			return groupNumber;
		}
		
		/**
		 * Returns the group name of the
		 * <a href="Pattern.html#groupname">named-capturing group</a> that was
		 * captured
		 *
		 * @return The group name of the
		 * <a href="Pattern.html#groupname">named-capturing group</a> that
		 * was captured or null if this isn't a
		 * <a href="Pattern.html#groupname">named-capturing group</a>
		 */
		public String getGroupName () {
			return groupName;
		}
		
		/**
		 * Returns a {@link Capture} object that contains the start and end index of
		 * the region of the input sequence captured
		 *
		 * @return A {@link Capture} object that contains the start and end index of
		 * the region of the input sequence captured
		 */
		public Capture getCapture () {
			return capture;
		}
		
		/**
		 * Returns the children of this node
		 *
		 * @return The children of this node
		 */
		public List<CaptureTreeNode> getChildren () {
			return children;
		}
		
		/**
		 * Returns the parent of this node
		 *
		 * @return The parent of this node
		 */
		public CaptureTreeNode getParent () {
			return parent;
		}
		
		/**
		 * Returns whether this capture is from a
		 * <a href="Pattern.html#recursion">recursive group call</a>
		 *
		 * @return Whether this capture is from a
		 * <a href="Pattern.html#recursion">recursive group call</a>
		 */
		public boolean isRecursion () {
			return recursion;
		}
		
		@Override
		public String toString () {
			StringBuilder sb = new StringBuilder ();
			toString (sb, 0);
			return sb.toString ();
		}
		
		public void setGroupName (Map<Integer, String> groupNames) {
			
			groupName = groupNames.get (groupNumber);
			
			for (CaptureTreeNode ctn : children)
				ctn.setGroupName (groupNames);
			
		}
		
		protected void toString (StringBuilder sb, int numTabs) {
			
			sb.append ("\t".repeat (Math.max (0, numTabs)));
			sb.append (groupName != null ? groupName : groupNumber);
			sb.append ("\n");
			
			for (CaptureTreeNode ctn : children)
				ctn.toString (sb, numTabs + 1);
			
		}
		
		public Capture findGroup (int group) {
			return findGroup (group, true);
		}
		
		protected Capture findGroup (int group, boolean searchParent) {
			
			ListIterator<CaptureTreeNode> it = children.listIterator (children.size ());
			
			Capture c = findGroup (group, it);
			
			if (c != null) return c;
			
			if (searchParent) {
				
				CaptureTreeNode current = this;
				
				while (!current.recursion && current.parent != null) {
					
					current = current.parent;
					it = current.children.listIterator (current.children.size () - 1);
					
					c = findGroup (group, it);
					
					if (c != null)
						return c;
					
				}
				
			}
			
			return null;
			
		}
		
		protected Capture findGroup (int group, ListIterator<CaptureTreeNode> it) {
			while (it.hasPrevious ()) {
				CaptureTreeNode child = it.previous ();
				if (child.groupNumber == group)
					return child.capture;
				if (child.recursion)
					continue;
				Capture c = child.findGroup (group, false);
				if (c != null)
					return c;
			}
			return null;
		}
		
		public void shrinkChildrenTo (int size) {
			
			if (children.size () > size)
				children.subList (size, children.size ()).clear ();
			
		}
		
	}