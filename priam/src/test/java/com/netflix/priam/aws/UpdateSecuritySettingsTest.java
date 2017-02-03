package com.netflix.priam.aws;

import com.google.common.collect.ImmutableList;
import com.netflix.priam.FakeMembership;
import com.netflix.priam.IConfiguration;
import com.netflix.priam.identity.IMembership;
import com.netflix.priam.identity.IPriamInstanceFactory;
import com.netflix.priam.identity.PriamInstance;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@RunWith(JMockit.class)
public class UpdateSecuritySettingsTest {
    private UpdateSecuritySettings updateSecuritySettings;
    private @Mocked IConfiguration config;
    private FakeMembership membership;
    private @Mocked IPriamInstanceFactory factory;

    private final String appName = "myApp";
    private final String hostIp1 = "1.2.3.4";
    private final String hostIp2 = "1.2.3.5";

    @Before
    public void setUp()
    {
        membership = new FakeMembership();
        updateSecuritySettings = new UpdateSecuritySettings(config, membership, factory);

        new Expectations() {
            {
                config.getAppName(); result = appName;
                config.getSSLStoragePort(); result = 7103;
            }
        };
    }

    @After
    public void tearDown() throws MalformedObjectNameException, MBeanRegistrationException, InstanceNotFoundException
    {
        //Remove registered UpdateSecuritySettings MBean object to reset it for testing
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName ussObjectName = new ObjectName("com.priam.scheduler:type=com.netflix.priam.aws.UpdateSecuritySettings");
        mbs.unregisterMBean(ussObjectName);
    }

    @Test
    public void SSLStoragePortIsAddedToACL(@Mocked final PriamInstance instance1,
                                           @Mocked final PriamInstance instance2) throws Exception {

        final HashMap<Integer, ArrayList<String>> expectedACLMap = new HashMap<>();
        expectedACLMap.put(7103, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));

        new Expectations() {
            {
                factory.getAllIds(appName); result = ImmutableList.of(instance1, instance2);
                instance1.getHostIP(); result = hostIp1;
                instance2.getHostIP(); result = hostIp2;
            }
        };

        this.updateSecuritySettings.execute();
        Assert.assertEquals(expectedACLMap, membership.getACLMap());
    }

    @Test
    public void OldHostIsRemovedFromACL(@Mocked final PriamInstance instance1) throws Exception {

        //Add two hosts to ACLs
        membership.addACL(Arrays.asList(hostIp1, hostIp2), 7103, 7103);

        final HashMap<Integer, ArrayList<String>> expectedACLMap = new HashMap<>();
        expectedACLMap.put(7103, new ArrayList<>(Arrays.asList(hostIp1 + "/32")));

        new Expectations() {
            {
                factory.getAllIds(appName); result = ImmutableList.of(instance1);
                instance1.getHostIP(); result = hostIp1;
            }
        };

        this.updateSecuritySettings.execute();
        Assert.assertEquals(expectedACLMap, membership.getACLMap());
    }

    @Test
    public void AdditionalPortsAreAddedToACL(@Mocked final PriamInstance instance1,
                                             @Mocked final PriamInstance instance2) throws Exception {

        final HashMap<Integer, ArrayList<String>> expectedACLMap = new HashMap<>();
        expectedACLMap.put(7103, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));
        expectedACLMap.put(7104, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));
        expectedACLMap.put(7105, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));

        new Expectations() {
            {
                config.getAdditionalPorts(); result = new HashSet<>(Arrays.asList(7104,7105));
                factory.getAllIds(appName); result = ImmutableList.of(instance1, instance2);
                instance1.getHostIP(); result = hostIp1;
                instance2.getHostIP(); result = hostIp2;
            }
        };

        this.updateSecuritySettings.execute();
        Assert.assertEquals(expectedACLMap, membership.getACLMap());
    }

    @Test
    public void ACLIsUpdatedOnChange(@Mocked final PriamInstance instance1,
                                     @Mocked final PriamInstance instance2) throws Exception {

        final HashMap<Integer, ArrayList<String>> expectedACLMap = new HashMap<>();
        expectedACLMap.put(7103, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));
        expectedACLMap.put(7105, new ArrayList<>(Arrays.asList(hostIp1 + "/32", hostIp2 + "/32")));

        new Expectations() {
            {
                factory.getAllIds(appName); result = ImmutableList.of(instance1, instance2);
                instance1.getHostIP(); result = hostIp1;
                instance2.getHostIP(); result = hostIp2;
                config.getAdditionalPorts(); returns(
                    new HashSet<>(Arrays.asList(7104)),
                    new HashSet<>(Arrays.asList(7105)));
            }
        };

        //Add port 7103 and 7104 to ACL
        this.updateSecuritySettings.execute();

        //Remove port 7104 and add 7105
        this.updateSecuritySettings.execute();

        //Assert only 7103 and 7105 remains
        Assert.assertEquals(expectedACLMap, membership.getACLMap());
    }
}