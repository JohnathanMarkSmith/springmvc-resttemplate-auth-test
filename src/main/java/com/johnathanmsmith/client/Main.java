package com.johnathanmsmith.client;


import com.johnathanmsmith.mvc.web.error.ErrorHolder;
import com.johnathanmsmith.mvc.web.model.RESTServer;
import com.johnathanmsmith.mvc.web.model.User;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.log4j.Logger.getLogger;

@PropertySource("classpath:application.properties")
public class Main
{

    /**
     * Setting up logger
     */
    private static final Logger LOGGER = getLogger(Main.class);


    public static void main(String[] args) throws IOException
    {
        LOGGER.debug("Starting REST Client!!!!");

        /**
         *
         * This is going to setup the REST server configuration in the applicationContext
         * you can see that I am using the new Spring's Java Configuration style and not some OLD XML file
         *
         */
        ApplicationContext context = new AnnotationConfigApplicationContext(RESTConfiguration.class);

        /**
         *
         * We now get a RESTServer bean from the ApplicationContext which has all the data we need to
         * log into the REST service with.
         *
         */
        RESTServer mRESTServer = context.getBean(RESTServer.class);

        /**
         *
         * Setting up BASIC Authentication access
         *
         */

         HttpClient client = new HttpClient();
        UsernamePasswordCredentials credentials =
                new UsernamePasswordCredentials(mRESTServer.getUser(), mRESTServer.getPassword());

        client.getState().setCredentials(
                new AuthScope(mRESTServer.getHost(), 8080, AuthScope.ANY_REALM),
                credentials);

        CommonsClientHttpRequestFactory commons = new CommonsClientHttpRequestFactory(client);



        /**
         *
         * Setting up data to be sent to REST service
         *
         */
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("id", "INID");

        /**
         *
         * Doing the REST call and then displaying the data/user object
         *
         */
        try
        {

            /*

                This is code to post and return a user object

             */

            RestTemplate rt = new RestTemplate(commons); // Added the CommonsClientHttpRequestFactory

            rt.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
            rt.getMessageConverters().add(new StringHttpMessageConverter());

            String uri = new String("http://" + mRESTServer.getHost() + ":8080/springmvc-resttemplate-auth-test/api/{id}");

            User u = new User();
            u.setName("Johnathan M Smith");
            u.setUser("JMS");


            User returns = rt.postForObject(uri, u, User.class, vars);

            LOGGER.debug("User:  " + u.toString());

        }
        catch (HttpClientErrorException e)
        {
            /**
             *
             * If we get a HTTP Exception display the error message
             */

            LOGGER.error("error:  " + e.getResponseBodyAsString());

            ObjectMapper mapper = new ObjectMapper();
            ErrorHolder eh = mapper.readValue(e.getResponseBodyAsString(), ErrorHolder.class);

            LOGGER.error("error:  " + eh.getErrorMessage());

        }
        catch(Exception e)
        {
            LOGGER.error("error:  " + e.getMessage());

        }
    }

}
