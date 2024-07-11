  /*
 * Copyright (c) 2023 UPL Foundation
 */
  
  package upl.app;
  
  import upl.app.MyLocalization;
  
  public class Russian extends MyLocalization {
    
    @Override
    public String getCode () {
      return "ru";
    }
    
    public String buttonSubmit () {
      return "Подтвердить";
    }
    
  }