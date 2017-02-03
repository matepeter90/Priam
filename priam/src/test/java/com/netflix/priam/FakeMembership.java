package com.netflix.priam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.netflix.priam.identity.IMembership;

public class FakeMembership implements IMembership
{

    private List<String> instances;
    private HashMap<Integer, List<String>> aclMap;

    public FakeMembership()
    {
         aclMap = new HashMap<>();
    }

    public FakeMembership(List<String> priamInstances)
    {
        this();
        this.instances = priamInstances;
    }
    
    public void setInstances( List<String> priamInstances)
    {
        this.instances = priamInstances;
    }

    @Override
    public List<String> getRacMembership()
    {
        return instances;
    }
    
    @Override
    public List<String> getCrossAccountRacMembership()
    {
       return null;	
    }


    @Override
    public int getRacMembershipSize()
    {
        return 3;
    }

    @Override
    public int getRacCount()
    {
        return 3;
    }

    @Override
    public void addACL(Collection<String> listIPs, int from, int to)
    {
        if(aclMap.get(from) == null)
            aclMap.put(from, new ArrayList<String>());
        aclMap.get(from).addAll(listIPs);
    }

    @Override
    public void removeACL(Collection<String> listIPs, int from, int to)
    {
        if(aclMap.get(from) != null)
        {
            aclMap.get(from).removeAll(listIPs);
            if(aclMap.get(from).isEmpty())
            {
                aclMap.remove(from);
            }
        }
    }

    @Override
    public List<String> listACL(int from, int to)
    {
        return aclMap.get(from);
    }

    @Override
    public HashMap<Integer, List<String >> getACLMap()
    {
        return aclMap;
    }

    public void clearACLMap()
    {
        aclMap.clear();
    }

    @Override
    public void expandRacMembership(int count)
    {
        // TODO Auto-generated method stub
        
    }
}
