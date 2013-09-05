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
                <actions>
                    <action>create</action>
                    <action>publish</action>
                </actions>
                <componentName>My Component</componentName>
                <componentType>J2EE:5.5.0.0</componentType>
                <enablerName>Tomcat 6.0 Enabler</enablerName>
                <enablerVersion>5.5</enablerVersion>
                <description>My New Component</description>
                <defaultSettings>
                    <defaultSetting>
                        <name>Default Priority</name>
                        <value>5</value>
                    </defaultSetting>
                </defaultSettings>
                <options>
                    <option>
                        <name>Maximum Activation Time in seconds</name>
                        <value>60</value>
                    </option>
                </options>
                <runtimeContextVariables>
                    <runtimeContextVariable>
                        <name>Example RCV</name>
                        <value>bar</value>
                        <type>1</type>
                        <export>true</export>
                    </runtimeContextVariable>
                </runtimeContextVariables>
                <features>
                    <feature>
                        <name>HTTP Support</name>
                        <description>Support for HTTP routing</description>
                    </feature>
                </features>
            </configuration>
        </execution>
    </executions>
</plugin>
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
