package external;

import com.intuit.karate.junit5.Karate;

class ExternalRunner {
    
    @Karate.Test
    Karate testPrincipal() {
        return Karate.run("principal").relativeTo(getClass());
    }    

    // @Karate.Test
    // Karate testWs() {
    //     return Karate.run("ws").relativeTo(getClass());
    // }  
}
