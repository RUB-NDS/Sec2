package org.sec2.extern;


import org.apache.commons.lang.StringUtils;
/**
 * Hello world!
 *
 */
public class App
 
{
    public static void main( String[] args )
    {
 
        System.out.println( "Hello World!");
    }
    
    public static void sayHelloAgain() {
		System.out.println(StringUtils.swapCase("MyTest says hello again!"));
	}
}
