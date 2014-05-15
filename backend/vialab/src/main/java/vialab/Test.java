package vialab;

import static spark.Spark.*;

import spark.*;

public class Test {

   public static void main(String[] args) {

	  
      get(new Route("/hello") {
         @Override
         public Object handle(Request request, Response response) {
            return "Hello World!";
         }
      });

      get(new Route("/hello/:name") {
    	    @Override
    	    public Object handle(Request request, Response response) {
    	       return "Hello: " + request.params(":name");
    	    }
    	 });       
      
      
      
      
   }

}
