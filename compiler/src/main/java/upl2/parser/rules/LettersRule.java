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
  
  package upl2.parser.rules;
  
  import upl2.parser.Parser;
  
  public class LettersRule extends Parser.Rule {
    
    public LettersRule (Quantifier quantifier, Case cond, String token) {
      
      this.quantifier = quantifier;
      this.cond = cond;
      this.token = token;
      //this.listener = listener;
      
    }
    
    @Override
    protected void ruleProcess () {
    }
    
  }