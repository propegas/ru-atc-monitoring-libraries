package ru.atc.zabbix.general;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.atc.zabbix.general.HashGenerator.hashString;

//import static ru.atc.camel.zabbix.general.HashGenerator.hashString;

//import ru.atc.camel.zabbix.devices.ZabbixAPIConsumer;

/**
 * Created by vgoryachev on 01.03.2016 ${PACKAGE_NAME}.
 * Package: ${PACKAGE_NAME}.
 */
public class CiItems {
    private static Logger logger = LoggerFactory.getLogger(CiItems.class);

    public static String checkHostPattern(String hostHost, String hostName) {
        String ciHostAliasName = null;
        // Example: KRL-PHOBOSAU--MSSQL
        //String[] hostreturn = new String[] { "", "" } ;
        Pattern p = Pattern.compile("(.*)--(.*)");

        Matcher hostHostMatcher = p.matcher(hostHost.toUpperCase());
        Matcher hostNameMatcher = p.matcher(hostName.toUpperCase());
        //String output = "";
        if (hostHostMatcher.matches()) {
            logger.debug("*** Use hostHost for alias name: " + hostHost.toUpperCase());
            ciHostAliasName = hostHost;
        } else if (hostNameMatcher.matches()) {
            logger.debug("*** Use hostName for alias name: " + hostName.toUpperCase());
            ciHostAliasName = hostName;
        }
        return ciHostAliasName;
    }

    public static String[] checkItemForCi(String itemname, String hostid, String hostname,
                                          String itemCiPattern, String itemCiParentPattern, String itemCiTypePattern) {

        //logger.debug("*** Received Zabbix Item : " + itemname);

        // Example item as CI :
        // [test CI item] bla-bla
        // [CI 2 (TYPE)::CI 3 (TYPE)] trapper item status

        // zabbix_item_ke_pattern=\\[(.*)\\](.*)
        Pattern itemWithCiPattern = Pattern.compile(itemCiPattern);

        // zabbix_item_ci_parent_pattern=(.*)::(.*)
        Pattern ciWithParentPattern = Pattern.compile(itemCiParentPattern);

        // zabbix_item_ci_type_pattern=(.*)\\((.*)\\)
        Pattern ciWithTypePattern = Pattern.compile(itemCiTypePattern);

        Matcher matcher = itemWithCiPattern.matcher(itemname);
        String ciid;
        String newitemname = "";
        String ciname;
        String devicetype = "";
        String parentitem;
        String parentid = "";
        //String hostnameend = "";

        // if Item has CI pattern
        // [CI 2 (TYPE)::CI 3 (TYPE)] trapper item status
        if (matcher.matches()) {

            logger.debug("*** Finded Zabbix Item with Pattern as CI: " + itemname);

            // save as CI name
            newitemname = matcher.group(1).toUpperCase();

            ciname = newitemname;

            logger.debug("*** newitemname: " + newitemname);

            // CI 2 (TYPE)::CI 3 (TYPE)
            Matcher matcher2 = ciWithParentPattern.matcher(newitemname);
            if (matcher2.matches()) {
                logger.debug("*** Finded Zabbix Item with Pattern with Parent: " + newitemname);
                newitemname = matcher2.group(2).trim().toUpperCase();

                ciname = newitemname;

                parentitem = matcher2.group(1).trim().toUpperCase();
                logger.debug("*** newitemname: " + newitemname);
                logger.debug("*** parentitem: " + parentitem);

                logger.debug(String.format("*** Trying to generate hash for ParentItem with Pattern: %s:%s",
                        hostname, parentitem));
                String hash = "";
                try {
                    hash = hashString(String.format("%s:%s", hostname, parentitem), "SHA-1");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logger.debug("*** Generated Hash: " + hash);
                parentid = hash;

            }

            // CI 2 (TYPE)
            Matcher matcher3 = ciWithTypePattern.matcher(newitemname);
            if (matcher3.matches()) {
                logger.debug("*** Finded Zabbix Item with Pattern with Type: " + newitemname);
                newitemname = matcher3.group(1).trim().toUpperCase();
                devicetype = matcher3.group(2).trim().toUpperCase();
                logger.debug("*** newitemname: " + newitemname);
                logger.debug("*** devicetype: " + devicetype);
            }

            // get SHA-1 hash for hostname-item block for saving as ciid
            // Example:
            // KRL-PHOBOSAU--PHOBOS:TEST CI ITEM
            logger.debug(String.format("*** Trying to generate hash for Item with Pattern: %s:%s",
                    hostname, ciname));
            String hash = "";
            try {
                hash = hashString(String.format("%s:%s", hostname, ciname), "SHA-1");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("*** Generated Hash: " + hash);
            ciid = hash;

            //event.setParametr(itemname);
        }
        // if Item has no CI pattern
        else {
            logger.debug("*** Use parent host as CI for item: " + itemname);
            ciid = hostid;

        }

        // id, name, type, parentid
        String[] hostreturn = new String[]{"", "", "", ""};
        hostreturn[0] = ciid;
        hostreturn[1] = newitemname;
        hostreturn[2] = devicetype;
        hostreturn[3] = parentid;
        //hostreturn[1] = hostnameend;

        logger.debug("New Zabbix CI ID: " + hostreturn[0]);
        logger.debug("New Zabbix CI Name: " + hostreturn[1]);
        logger.debug("New Zabbix DeviceType: " + hostreturn[2]);
        logger.debug("New Zabbix ParentID: " + hostreturn[3]);

        return hostreturn;
    }

    public static String[] checkHostAliases(JSONArray allZabbixHosts, String hostHost, String hostName) {

        // Example: KRL-PHOBOSAU--MSSQL
        String[] hostreturn = new String[]{"", "", ""};
        Pattern p = Pattern.compile("(.*)--(.*)");

        logger.debug("*** Check hostHost or hostName for aliases: " + hostHost.toUpperCase());

        String hostnameBeginPart = "";
        String hostnameEndPart = "";
        //String output = "";

        String host = CiItems.checkHostPattern(hostHost, hostName);
        Matcher hostMatcher = p.matcher(host.toUpperCase());
        if (hostMatcher.matches()) {
            hostnameEndPart = hostMatcher.group(2).toUpperCase();
            hostnameBeginPart = hostMatcher.group(1).toUpperCase();
            logger.debug("*** hostnameBeginPart: " + hostnameBeginPart);
            logger.debug("*** hostnameEndPart: " + hostnameEndPart);
        }

        //else return
        //hostgroupsloop:
        String ParentID = "";

        if (allZabbixHosts != null && 0 != allZabbixHosts.size()) {
            int j = 0;
            while (j < allZabbixHosts.size()) {
                // logger.debug(f.toString());
                // ZabbixAPIHost host = new ZabbixAPIHost();
                JSONObject host_a = allZabbixHosts.getJSONObject(j);
                String compareHost = host_a.getString("host");
                logger.debug("*** Compare with hosthost: " + compareHost);
                if (compareHost.equalsIgnoreCase(hostnameBeginPart)) {
                    ParentID = host_a.getString("hostid");
                    break;
                }

                String compareName = host_a.getString("name");
                logger.debug("*** Compare with hostname: " + compareName);
                if (compareName.equalsIgnoreCase(hostnameBeginPart)) {
                    ParentID = host_a.getString("hostid");
                    break;

                }
                j++;
            }
        }

        hostreturn[0] = ParentID;
        hostreturn[1] = hostnameEndPart;
        hostreturn[2] = hostnameBeginPart;

        logger.debug("New Zabbix Host ParentID: " + hostreturn[0]);
        logger.debug("New Zabbix Host Name: " + hostreturn[1]);

        return hostreturn;
    }

    public static String getTransformedItemName(String name, String key) {

        // get params from key to item $1 placeholder
        // Example:
        // vfs.fs.size[/oracle,pfree]

        Pattern p = Pattern.compile("(.*)\\[(.*)\\]");
        Matcher matcher = p.matcher(key);

        String keyparams;
        //String webscenario = "";
        //String webstep = "";

        String[] params = new String[]{};

        if (matcher.matches()) {

            logger.debug("*** Finded Zabbix Item key with $1-$9 placeholders Pattern: " + key);
            // vfs.fs.size[/oracle,pfree]
            keyparams = matcher.group(2);

            // [/oracle,pfree]
            params = keyparams.split(",");
            logger.debug(String.format("*** Finded Zabbix Item key params (size): %d ", params.length));


        }
        // if Item has no $1 placeholders pattern
        else {

            logger.debug("*** Zabbix $1-$9 placeholders Pattern not found fro key: " + key);
        }

        logger.debug("Item name: " + name);

        String param;
        int paramnumber;
        Matcher m = Pattern.compile("\\$\\d+").matcher(name);
        while (m.find()) {
            param = m.group(0);
            paramnumber = Integer.parseInt(param.substring(1));
            logger.debug("Found Param: " + paramnumber);
            logger.debug("Found Param Value: " + param);
            logger.debug("Found Param Value Replace: " + params[paramnumber - 1]);

            name = name.replaceAll("\\$" + paramnumber, params[paramnumber - 1]);

        }


        logger.debug("New Zabbix Item Name: " + name);

        return name;

    }
}
