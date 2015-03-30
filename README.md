[fabrician.org](http://fabrician.org/)
==========================================================================
Silver Fabric Maven Plugin
==========================================================================

Maven Plugin for creating Silver Fabric Components and Stacks.

## Help

`silverfabric:help`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:help`

#### Description:

Display help for the supported goals.

`mvn silverfabric:help -Ddetail=true -Dgoal=stacks`


## Components

`silverfabric:components`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:components`

#### Description:

Perform the following actions on Components: publish, unpublish, update, delete, get info, get archives, add archives, remove archive, remove archives, assign to non cloud, get config file, update config file, delete config file, get content file path, get content file path with regexp, delete content file path with regex, add content file, get http-urls, delete http-urls, add http-urls, update http-urls, auto-detect http-urls, get patches, get script-files, add script-files, get script-files content, update script-files content, remove script-files, get script-files path with regex, delete script-files path with regex, get type names, get types, get, clean.

#### Example:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.fedex.scm.sf</groupId>
  <artifactId>silverfabric-maven-plugin-test</artifactId>
  <version>0.16</version>

  <packaging>jar</packaging>

  <name>Silver Fabric Maven Plugin Test</name>
  <description>Maven Plugin for creating Silver Fabric Components Test.</description>

  <build>
    <plugins>
      <plugin>
        <groupId>com.tibco.silverfabric</groupId>
        <artifactId>silverfabric-maven-plugin</artifactId>
        <version>0.16</version>
        <configuration>
          <brokerConfig>
            <brokerURL>http://localhost:8080</brokerURL>
            <username>admin</username>
            <password>admin</password>
          </brokerConfig>
        </configuration>
        <executions>
          <execution>
            <id>Create Component</id>
            <phase>install</phase>
            <goals>
              <goal>components</goal>
            </goals>
            <configuration>
              <plan>plan-1.xml</plan>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

Where the plan consists of the configuration that needs to be created.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<component xmlns="http://www.example.org/SilverLining"
  xmlns:tns="http://admin.fabric.webservices.datasynapse.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.example.org/SilverLining SilverLining.xsd ">
  
  
  <enablerName>enablerName</enablerName>
  <enablerVersion>enablerVersion</enablerVersion>
  <componentType>componentType</componentType>
  <MiddlewarVersions>MiddlewarVersions</MiddlewarVersions>
  <Department>Department</Department>
  <Location>Location</Location>
  <Partition>Partition</Partition>
  <EngineBlacklisting>EngineBlacklisting</EngineBlacklisting>
  <FailuresPerDayBeforeBlacklist>FailuresPerDayBeforeBlacklist</FailuresPerDayBeforeBlacklist>

  <feature>
    <tns:description>Application Logging Support</tns:description>
    <tns:infoDescription>Support for handling of custom application logs
    </tns:infoDescription>
    <tns:name>applicationloggingsupport</tns:name>
    <tns:properties>
      <tns:name>Checkpoint Frequency In Seconds</tns:name>
      <tns:value>300</tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>Archive Application Logs</tns:name>
      <tns:value>true</tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>Log File Pattern</tns:name>
      <tns:value>/../domaindata/tra/${TIBCO_DOMAIN_NAME}/logs/*.log,/./tibco/tra/5.8/logs/*.log,/../domaindata/tra/${TIBCO_DOMAIN_NAME}/application/logs/.*.log
      </tns:value>
    </tns:properties>
  </feature>
  <feature>
    <tns:description>HTTP Support</tns:description>
    <tns:infoDescription>Support for HTTP routing
    </tns:infoDescription>
    <tns:name>httpsupport</tns:name>
    <tns:properties>
      <tns:name>Relative Url</tns:name>
      <tns:value></tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>Routing Prefix</tns:name>
      <tns:value></tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>HTTPS Enabled</tns:name>
      <tns:value>false</tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>HTTP Enabled</tns:name>
      <tns:value>true</tns:value>
    </tns:properties>
    <tns:properties>
      <tns:name>Routing Directly To Endpoints</tns:name>
      <tns:value>false</tns:value>
    </tns:properties>
  </feature>

  <runtime-variable>
    <tns:autoIncrementType>0</tns:autoIncrementType>
    <tns:description>External Jar Files</tns:description>
    <tns:export>false</tns:export>
    <tns:name>WITHOUT_TRA</tns:name>
    <tns:type>1</tns:type>
    <tns:value>false</tns:value>
  </runtime-variable>


  <allocation-rule>
    <tns:condition>
      <tns:description>tns:description</tns:description>
      <tns:properties>
        <tns:name>tns:name</tns:name>
        <tns:value>tns:value</tns:value>
      </tns:properties>
      <tns:type>tns:type</tns:type>
    </tns:condition>
    <tns:description>tns:description</tns:description>
    <tns:properties>
      <tns:name>tns:name</tns:name>
      <tns:value>tns:value</tns:value>
    </tns:properties>
    <tns:ruleAction>tns:ruleAction</tns:ruleAction>
    <tns:type>tns:type</tns:type>
  </allocation-rule>
  <allocation-constraint>
    <currentValue>0</currentValue>
    <description>tns:description</description>
    <lastModified>0</lastModified>
    <maxValue>0</maxValue>
    <modifiedBy>tns:modifiedBy</modifiedBy>
    <name>tns:name</name>
    <type>tns:type</type>
  </allocation-constraint>
</component>
```


## Stacks

`silverfabric:stacks`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:stacks`

#### Description:

Perform the following actions on Stacks: publish, unpublish, update, delete, get info, assign to non cloud, get config file, set run mode, auto-detect http-urls, get patches, get script-files, get type names, get types, get, clean.

#### Example:

```xml
<plugin>
    <groupId>com.tibco.silverfabric</groupId>
    <artifactId>silverfabric-maven-plugin</artifactId>
    <version>0.16</version>
    <configuration>
        <brokerConfig>
            <brokerURL>http://localhost:8080</brokerURL>
            <username>admin</username>
            <password>admin</password>
        </brokerConfig>
    </configuration>
    <executions>
        <execution>
            <id>My Example Stack</id>
            <phase>install</phase>
            <goals>
                <goal>stacks</goal>
            </goals>
            <configuration>
                <actions>
                    <action>create</action>
                    <action>publish</action>
                </actions>
                <stackName>My Example Stack</stackName>
                <components>
                    <component>My Component</component>
                </components>
                <propertyOverrides>
                    <propertyOverride>
                        <name>description</name>
                        <value>My Example Stack built by Maven</value>
                        <displayName>Stack Description</displayName>
                        <source></source>
                        <order>0</order>
                        <datatype>string</datatype>
                        <readonly>false</readonly>
                        <required>false</required>
                    </propertyOverride>
                    <propertyOverride>
                        <name>displayName</name>
                        <value>My Example Stack</value>
                        <displayName>Stack Name</displayName>
                        <source></source>
                        <order>0</order>
                        <datatype>string</datatype>
                        <readonly>false</readonly>
                        <required>true</required>
                        <validations>
                            <unique>owner</unique>
                            <message>This field cannot be empty</message>
                        </validations>
                    </propertyOverride>
                    <propertyOverride>
                        <name>Instance Count</name>
                        <value>1</value>
                        <displayName>My Component Instance Count</displayName>
                        <source>My Component</source>
                        <validations>
                            <max>1</max>
                            <min>1</min>
                        </validations>
                        <datatype>int</datatype>
                        <order>0</order>
                        <readonly>true</readonly>
                        <required>false</required>
                    </propertyOverride>
                </propertyOverrides>
                <policies>
                    <policy>
                        <componentAllocationInfo>
                            <componentAllocationInfoDetail>
                                <name>My Component</name>
                                <min>1</min>
                                <max>1</max>
                                <priority>5</priority>
                                <!--
                                <allocationRules>
                                    <allocationRule>
                                        <properties>
                                            <property>
                                                <name>component</name>
                                                <value>Some Other Component</value>
                                            </property>
                                            <property>
                                                <name>shutdown</name>
                                                <value>true</value>
                                            </property>
                                        </properties>
                                        <type>Component Dependency</type>
                                    </allocationRule>
                                </allocationRules>
                                -->
                            </componentAllocationInfoDetail>
                        </componentAllocationInfo>
                    </policy>
                </policies>
                <templateLevel>local</templateLevel>
                <owner>admin</owner>
                <description>My Example Stack built by Maven</description>
                <technology>J2EE</technology>
                <icon>/livecluster/admin/images/icons/stackIcons/defaults/4_Skyway_J2EE_Default_Icon.png</icon>
                <mode>stopped</mode>
            </configuration>
        </execution>
    </executions>
</plugin>
```


## Grid Libararies

`silverfabric:gridlibs`

#### Full name:

`com.tibco.silverfabric:silverfabric-maven-plugin:0.16:gridlibs`

#### Description:

Needed

#### Example:
```xml
<plugin>
    <groupId>com.tibco.silverfabric</groupId>
    <artifactId>silverfabric-maven-plugin</artifactId>
    <version>0.16</version>
    <executions>
        <execution>
            <id>gridlib upload</id>
            <phase>test</phase>
            <goals>
                <goal>gridlibs</goal>
            </goals>
            <configuration>
                <actions>
                    <action>add</action>
                </actions>
                <archives>
                    <archive>
                        <path>/opt/tibco/SilverFabric/5.5/fabric/webapps/livecluster/deploy/resources/gridlib/</path>
                        <name>SilverFabric_as_2.0.2.66_linux24gl23_x86_64_distribution_gridlib.tar.gz</name>
                    </archive>
                </archives>
                <!--<overWrite>true</overWrite>-->
            </configuration>
        </execution>
    </executions>
</plugin>
```
