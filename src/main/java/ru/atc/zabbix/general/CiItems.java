package ru.atc.zabbix.general;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vgoryachev on 01.03.2016 ${PACKAGE_NAME}.
 * Package: ${PACKAGE_NAME}.
 */
public class CiItems {
    private static Logger logger = LoggerFactory.getLogger(CiItems.class);

    private CiItems() {
    }

    public static String checkHostPattern(String hostHost, String hostName) {
        return checkHostPattern(hostHost, hostName, "(.*)--(.*)");
    }

    public static String checkHostPattern(String hostHost, String hostName, String hostAliasPattern) {
        String ciHostAliasName = null;
        // Example: KRL-PHOBOSAU--MSSQL
        Pattern p = Pattern.compile(hostAliasPattern);

        Matcher hostHostMatcher = p.matcher(hostHost.toUpperCase());
        Matcher hostNameMatcher = p.matcher(hostName.toUpperCase());

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
                                          String itemCiPattern, String itemCiParentPattern,
                                          String itemCiTypePattern) throws Exception {

        // Example item as CI :
        // [test CI item] bla-bla
        // [CI 2 (TYPE)::CI 3 (TYPE)] trapper item status
        String checkItemName = itemname;

        // zabbix_item_ke_pattern=\\[(.*)\\](.*)
        Pattern itemWithCiPattern = Pattern.compile(itemCiPattern);

        // zabbix_item_ci_parent_pattern=(.*)::(.*)
        Pattern ciWithParentPattern = Pattern.compile(itemCiParentPattern);

        // zabbix_item_ci_type_pattern=(.*)\\((.*)\\)
        Pattern ciWithTypePattern = Pattern.compile(itemCiTypePattern);

        String ciId;
        String newItemName;
        String ciName;
        String deviceType = "";
        String parentItemName = "";
        String parentCiId = "";

        // WEB items
        Pattern p = Pattern.compile(".*((of|for) scenario)\\s\"(\\[(.*)](.*))\"\\.?");
        Matcher webMatcher = p.matcher(checkItemName);
        logger.debug("*** Check WEB test item: {}", checkItemName);

        if (webMatcher.matches()) {
            logger.debug("*** Found WEB test item: {}", checkItemName);
            String group = webMatcher.group(3);

            if (itemWithCiPattern.matcher(group).matches()) {
                logger.debug("*** Found item '{}' with CI Pattern in WEB test", group);
                checkItemName = group;
            }

        }

        Matcher matcher = itemWithCiPattern.matcher(checkItemName);
        // if Item has CI pattern
        // [CI 2 (TYPE)::CI 3 (TYPE)] trapper item status
        logger.debug("*** Using CI pattern: " + itemWithCiPattern);
        if (matcher.matches()) {

            logger.debug("*** Finded Zabbix Item with Pattern as CI: " + checkItemName);

            // save as CI name
            newItemName = matcher.group(1).toUpperCase();
            ciName = newItemName;

            logger.debug("*** newitemname: " + newItemName);

            // CI 2 (TYPE)::CI 3 (TYPE)
            Matcher matcher2 = ciWithParentPattern.matcher(newItemName);
            if (matcher2.matches()) {
                logger.debug("*** Finded Zabbix Item with Pattern with Parent: " + newItemName);

                // get name from ::parent
                newItemName = matcher2.group(2).trim().toUpperCase();
                ciName = newItemName;

                parentItemName = matcher2.group(1).trim().toUpperCase();
                logger.debug("*** newitemname: " + newItemName);
                logger.debug("*** parentitem: " + parentItemName);

                logger.debug(String.format("*** Trying to generate hash for ParentItem with Pattern: %s:%s",
                        hostname, parentItemName));
                String hash;
                //if ("".equals(parentItem))
                //hash = hashString(String.format("%s:%s", hostname, parentItemName), "SHA-1");
                hash = String.format("%s:%s", hostname, parentItemName);
               /* else
                    hash = hashString(String.format("%s:%s:%s", hostname, parentItem, parentItemName), "SHA-1");
              */  //hash = hashString(String.format("%s:%s", hostname, parentItemName), "SHA-1");

                logger.debug("*** Generated Hash: " + hash);
                parentCiId = hash;

            }

            // CI 2 (TYPE)
            try {
                Matcher matcher3 = ciWithTypePattern.matcher(newItemName);
                if (matcher3.matches()) {
                    logger.debug("*** Finded Zabbix Item with Pattern with Type: " + newItemName);
                    newItemName = matcher3.group(1).trim().toUpperCase();
                    deviceType = matcher3.group(2).trim().toUpperCase();
                    logger.debug("*** newitemname: " + newItemName);
                    logger.debug("*** devicetype: " + deviceType);
                }
            } catch (Exception e) {
                logger.error(String.format("Error while parsing CI type using regexp pattern: %s ", e));
            }

            // ci 2 | ci.id
            try {
                Pattern ciWithCiIdPattern = Pattern.compile("(.*) \\| (.*)");
                Matcher matcher4 = ciWithCiIdPattern.matcher(newItemName);
                if (matcher4.matches()) {
                    logger.debug("*** Finded Zabbix Item with Pattern with CI ID: " + newItemName);
                    newItemName = matcher4.group(1).trim().toUpperCase();
                    logger.debug("*** newitemname: " + newItemName);
                }
            } catch (Exception e) {
                logger.error(String.format("Error while parsing CI type using regexp pattern: %s ", e));
            }

            // get SHA-1 hash for hostname-item block for saving as ciid
            // Example:
            // KRL-PHOBOSAU--PHOBOS:TEST CI ITEM
            logger.debug(String.format("*** Trying to generate hash for Item with Pattern: %s:%s:%s",
                    hostname, parentItemName, ciName));
            String hash;
            //if ("".equals(parentItem))
            //hash = hashString(String.format("%s:%s", hostname, ciName), "SHA-1");
            hash = String.format("%s:%s", hostname, ciName);
           /* else
                hash = hashString(String.format("%s:%s:%s", hostname, parentItem, ciName), "SHA-1");
*/
            logger.debug("*** Generated Hash: " + hash);
            ciId = hash;

        }
        // if Item has no CI pattern
        else {
            logger.debug("*** No matches to CI pattern, use parent host as CI for item: " + checkItemName);
            ciId = hostid;
            newItemName = checkItemName;
        }

        // id, name, type, parentid
        String[] hostreturn = new String[]{"", "", "", "", ""};
        hostreturn[0] = ciId;
        hostreturn[1] = newItemName;
        hostreturn[2] = deviceType;
        hostreturn[3] = parentCiId;
        hostreturn[4] = parentItemName;

        logger.debug("New Zabbix CI ID: " + hostreturn[0]);
        logger.debug("New Zabbix CI Name: " + hostreturn[1]);
        logger.debug("New Zabbix DeviceType: " + hostreturn[2]);
        logger.debug("New Zabbix ParentID: " + hostreturn[3]);
        logger.debug("New Zabbix Parent CI Name: " + hostreturn[4]);

        return hostreturn;
    }

    public static String[] checkHostAliases(JSONArray allZabbixHosts, String hostHost,
                                            String hostName) {
        return checkHostAliases(allZabbixHosts, hostHost, hostName, "(.*)--(.*)");
    }

    public static String[] checkHostAliases(JSONArray allZabbixHosts, String hostHost,
                                            String hostName, String hostAliasPattern) {

        // Example: KRL-PHOBOSAU--MSSQL
        String[] hostreturn = new String[]{"", "", ""};
        Pattern p = Pattern.compile(hostAliasPattern);

        logger.debug("*** Check hostHost or hostName for aliases: " + hostHost.toUpperCase());

        String hostnameBeginPart = "";
        String hostnameEndPart = "";

        String host;
        host = CiItems.checkHostPattern(hostHost, hostName, hostAliasPattern);
        if (host == null)
            return hostreturn;

        Matcher hostMatcher = p.matcher(host.toUpperCase());
        if (hostMatcher.matches()) {
            hostnameEndPart = hostMatcher.group(2).toUpperCase();
            hostnameBeginPart = hostMatcher.group(1).toUpperCase();
            logger.debug("*** hostnameBeginPart: " + hostnameBeginPart);
            logger.debug("*** hostnameEndPart: " + hostnameEndPart);
        }

        String parentID = "";

        if (allZabbixHosts != null && !allZabbixHosts.isEmpty()) {
            int j = 0;
            while (j < allZabbixHosts.size()) {

                JSONObject comparingZabbixHost = allZabbixHosts.getJSONObject(j);

                String compareHost = comparingZabbixHost.getString("host");
                String compareName = comparingZabbixHost.getString("name");
                logger.debug("*** Compare with hosthost: " + compareHost);
                logger.debug("*** Compare with hostname: " + compareName);
                if (compareHost.equalsIgnoreCase(hostnameBeginPart) ||
                        compareName.equalsIgnoreCase(hostnameBeginPart)) {
                    parentID = comparingZabbixHost.getString("hostid");
                    break;
                }

                j++;
            }
        }

        hostreturn[0] = parentID;
        hostreturn[1] = hostnameEndPart;
        hostreturn[2] = hostnameBeginPart;

        logger.debug("New Zabbix Host ParentID: " + hostreturn[0]);
        logger.debug("New Zabbix Host Name: " + hostreturn[1]);

        return hostreturn;
    }

    public static String getTransformedItemName(String itemName, String key) {

        // get params from key to item $1 placeholder
        // Example:
        // vfs.fs.size[/oracle,pfree]

//        Pattern p = Pattern.compile("(.*)\\[(.*)\\]");
        Pattern p = Pattern.compile("([0-9a-zA-Z_\\-.]*)\\[(.*)\\]");
        Matcher matcher = p.matcher(key);

        String itemKeyParams;
        String[] params = new String[]{};

        if (matcher.matches()) {

            logger.debug("*** Finded Zabbix Item key with $1-$9 placeholders Pattern: " + key);
            // vfs.fs.size[/oracle,pfree]
            itemKeyParams = matcher.group(2);

            // [/oracle,pfree]
            params = itemKeyParams.split(",");
            logger.debug(String.format("*** Finded Zabbix Item key params (size): %d ", params.length));

        }
        // if Item has no $1 placeholders pattern
        else {
            logger.debug("*** Zabbix $1-$9 placeholders Pattern not found fro key: " + key);
        }

        logger.debug("Item name: " + itemName);
        String nameWithReplacedPlaceholders = itemName;

        String param;
        int paramIndex;
        Matcher m = Pattern.compile("\\$\\d+").matcher(itemName);
        while (m.find()) {
            param = m.group(0);
            paramIndex = Integer.parseInt(param.substring(1));
            logger.debug("Found Param: " + paramIndex);
            logger.debug("Found Param Value: " + param);
            logger.debug("Found Param Value Replace: " + params[paramIndex - 1]);

            nameWithReplacedPlaceholders = itemName.replaceAll("\\$" + paramIndex, params[paramIndex - 1]);

        }

        logger.debug("New Zabbix Item Name: " + nameWithReplacedPlaceholders);

        return nameWithReplacedPlaceholders;

    }
}
