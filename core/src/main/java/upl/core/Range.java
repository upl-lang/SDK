  /*
   * Copyright (c) 2020 - 2024 UPL Foundation
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */
  
  package upl.core;
  
  import java.lang.Math;
  import java.util.NoSuchElementException;
  import java.util.Iterator;
  
  public class Range implements Iterable<Integer> {
    
    protected int start, end;
    protected boolean stop = false;
    
    protected int[] part;
    
    public Range (int start, int end) {
      
      this.start = start;
      this.end = end;
      
    }
    
    @Override
    public Iterator<Integer> iterator () {
      return new RangeIterator (start, end);
    }
    
    public static class RangeIterator implements Iterator<Integer> {
      
      protected int start, end;
      
      public RangeIterator (int start, int end) {
        
        this.start = start;
        this.end = end;
        
      }
      
      @Override
      public boolean hasNext () {
        return start < end;
      }
      
      @Override
      public Integer next () {
        
        if (hasNext ())
          return start++;
        
        throw new NoSuchElementException ();
        
      }
      
      @Override
      public void remove () {
        throw new UnsupportedOperationException ();
      }
      
    }
    
    public boolean hasPart (int length) {
      
      if (!stop) {
        
        int part = Math.round (end / length); // 1000 / 10 = 100
        
        int end = (start + part);
        
        if (end < this.end) {
          
          this.part = new int [] { start, end };
          start += part;
          
          return true;
          
        } else {
          
          this.part = new int [] { start, this.end };
          stop = true;
          
          return true;
          
        }
        
      }
      
      return false;
      
    }
    
    public int[] getPart () {
      return part;
    }
    
  }