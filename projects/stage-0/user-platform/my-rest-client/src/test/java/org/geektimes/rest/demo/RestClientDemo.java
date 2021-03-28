package org.geektimes.rest.demo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class RestClientDemo {

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();
        Entity<String> entity = Entity.entity("{\n" +
                "  \"interface\": \"REPAY_PLAN_UPDATE_REPAY_ME\",\n" +
                "  \"SEQ_NO\": \"MHC1367668127906680856\",\n" +
                "  \"LOAN_ID\": \"LOAN00311000000717\"\n" +
                "}", MediaType.APPLICATION_JSON);
        Response response = client
                .target("https://gw-sti-o.maihaoche.net/yibin/business")
                .request()
                // .get();
                .post(entity);
        String content = response.readEntity(String.class);
        System.out.println(content);
    }
}
