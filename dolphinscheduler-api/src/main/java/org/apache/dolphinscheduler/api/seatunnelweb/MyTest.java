package org.apache.dolphinscheduler.api.seatunnelweb;

import java.io.IOException;

public class MyTest {

    public static void main(String[] args) throws IOException {
        SeaTunnelWebClient client = new SeaTunnelWebClient();
        String jobConfig = client.getJobConfig(2, 15697364989408L);
        System.out.println(jobConfig);

        String pageInfo = client.getJobDefinitionList("", 1, 100, "BATCH");

        System.out.println(pageInfo);
    }
}
