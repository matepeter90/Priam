package com.netflix.priam.resources;

import java.io.BufferedReader;
import java.io.FileReader;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class CassandraAdminTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private String readGossipInfo(String filePath) throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String result = "";
        while ((line = reader.readLine()) != null) {
            result += line + "\n";
        }
        reader.close();
        return result;
    }

    @Test
    public void parseGossipInfo() throws Exception
    {
        String result = readGossipInfo("src/test/resources/gossipinfo.txt");

        JSONObject rootObj = CassandraAdmin.parseGossipInfo(result);

        exception.expect(JSONException.class);
        //should not be found
        rootObj.get("ip-10-75-23-21.eu-west-1.compute.internal");

        Assert.assertNotNull(rootObj.get("10.80.147.144"));
        Assert.assertNotNull(rootObj.get("10.80.147.20"));
        Assert.assertNotNull(rootObj.get("10.75.23.21"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.147.144").get("SCHEMA").equals("bbb09269-73c9-36a4-b656-13470aa8a3fd"));
        Assert.assertTrue(rootObj.getJSONObject("10.75.23.21").get("SCHEMA").equals("bbb09269-73c9-36a4-b656-13470aa8a3fd"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.147.144").get("RPC_ADDRESS").equals("10.80.147.144"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.147.20").get("RPC_ADDRESS").equals("10.80.147.20"));
        Assert.assertTrue(rootObj.getJSONObject("10.75.23.21").get("RPC_ADDRESS").equals("10.75.23.21"));
        Assert.assertTrue(rootObj.getJSONObject("10.75.23.21").get("STATUS").equals("NORMAL"));
        Assert.assertTrue(rootObj.getJSONObject("10.75.23.21").get("Token").equals("372748112"));
        Assert.assertTrue(rootObj.getJSONObject("10.75.23.21").get("Token").equals("-9223372035046200208"));
        Assert.assertEquals(rootObj.length(), 3);
    }

    @Test
    public void parseGossipInfoV21() throws Exception
    {
        String result = readGossipInfo("src/test/resources/gossipinfo_v2.1.txt");

        JSONObject rootObj = CassandraAdmin.parseGossipInfo(result);

        exception.expect(JSONException.class);
        //should not be found
        rootObj.get("ip-10-80-142-40");

        Assert.assertNotNull(rootObj.get("10.80.142.40"));
        Assert.assertNotNull(rootObj.get("10.80.184.125"));
        Assert.assertNotNull(rootObj.get("10.80.227.119"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.142.40").get("SCHEMA").equals("3a7a0981-c1fa-3a6a-8bba-45aaa7c79d27"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.184.125").get("SCHEMA").equals("3a7a0981-c1fa-3a6a-8bba-45aaa7c79d27"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.142.40").get("RPC_ADDRESS").equals("10.80.142.40"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.184.125").get("RPC_ADDRESS").equals("10.80.184.125"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.227.119").get("RPC_ADDRESS").equals("10.80.227.119"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.142.40").get("STATUS").equals("NORMAL"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.142.40").get("Token").equals("-3074457343809683003"));
        Assert.assertTrue(rootObj.getJSONObject("10.80.184.125").get("Token").equals("3074457347426834202"));
        Assert.assertEquals(rootObj.length(), 3);
    }

}
