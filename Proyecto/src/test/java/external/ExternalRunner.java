package external;

import com.intuit.karate.junit5.Karate;

class ExternalRunner {

    @Karate.Test
    Karate testPreguntaB() {
        return Karate.run("preguntaB").relativeTo(getClass());
    }

    @Karate.Test
    Karate testUsage() {
        return Karate.run("usage").relativeTo(getClass());
    }

    
    // @Karate.Test
    // Karate testWs() {
    //     return Karate.run("ws").relativeTo(getClass());
    // }  
}
