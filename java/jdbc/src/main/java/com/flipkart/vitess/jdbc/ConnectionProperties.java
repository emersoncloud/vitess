package com.flipkart.vitess.jdbc;

import com.flipkart.vitess.util.Constants;
import com.flipkart.vitess.util.StringUtils;
import com.google.common.base.Function;
import com.youtube.vitess.proto.Query;
import com.youtube.vitess.proto.Topodata;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ConnectionProperties {

    private static final ArrayList<java.lang.reflect.Field> PROPERTY_LIST = new ArrayList<>();

    static {
        try {
            // Generate property list for use in dynamically filling out values from Properties objects in
            // #initializeProperties below
            java.lang.reflect.Field[] declaredFields = ConnectionProperties.class.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                if (ConnectionProperties.ConnectionProperty.class.isAssignableFrom(declaredFields[i].getType())) {
                    PROPERTY_LIST.add(declaredFields[i]);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Configs for handling deserialization of blobs
    private BooleanConnectionProperty blobsAreStrings = new BooleanConnectionProperty(
        "blobsAreStrings",
        "Should the driver always treat BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?",
        false);
    private BooleanConnectionProperty functionsNeverReturnBlobs = new BooleanConnectionProperty(
        "functionsNeverReturnBlobs",
        "Should the driver always treat data from functions returning BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?",
        false);

    // Configs for handing tinyint(1)
    private BooleanConnectionProperty tinyInt1isBit = new BooleanConnectionProperty(
        "tinyInt1isBit",
        "Should the driver treat the datatype TINYINT(1) as the BIT type (because the server silently converts BIT -> TINYINT(1) when creating tables)?",
        true);
    private BooleanConnectionProperty yearIsDateType = new BooleanConnectionProperty(
        "yearIsDateType",
        "Should the JDBC driver treat the MySQL type \"YEAR\" as a java.sql.Date, or as a SHORT?",
        true);

    // Configs for handling irregular blobs, those with characters outside the typical 4-byte encodings
    private BooleanConnectionProperty useBlobToStoreUTF8OutsideBMP = new BooleanConnectionProperty(
        "useBlobToStoreUTF8OutsideBMP",
        "Tells the driver to treat [MEDIUM/LONG]BLOB columns as [LONG]VARCHAR columns holding text encoded in UTF-8 that has characters outside the BMP (4-byte encodings), which MySQL server can't handle natively.",
        false);
    private StringConnectionProperty utf8OutsideBmpIncludedColumnNamePattern = new StringConnectionProperty(
        "utf8OutsideBmpIncludedColumnNamePattern",
        "Used to specify exclusion rules to \"utf8OutsideBmpExcludedColumnNamePattern\". The regex must follow the patterns used for the java.util.regex package.",
        null,
        null);
    private StringConnectionProperty utf8OutsideBmpExcludedColumnNamePattern = new StringConnectionProperty(
        "utf8OutsideBmpExcludedColumnNamePattern",
        "When \"useBlobToStoreUTF8OutsideBMP\" is set to \"true\", column names matching the given regex will still be treated as BLOBs unless they match the regex specified for \"utf8OutsideBmpIncludedColumnNamePattern\". The regex must follow the patterns used for the java.util.regex package.",
        null,
        null);

    // Default encodings, for when one cannot be determined from field metadata
    private StringConnectionProperty characterEncoding = new StringConnectionProperty(
        "characterEncoding",
        "If a character encoding cannot be detected, which fallback should be used when dealing with strings? (defaults is to 'autodetect')",
        null,
        null);

    // Vitess-specific configs
    private EnumConnectionProperty<Constants.QueryExecuteType> executeType = new EnumConnectionProperty<>(
        Constants.Property.EXECUTE_TYPE,
        "Query execution type: simple or stream",
        Constants.DEFAULT_EXECUTE_TYPE);
    private BooleanConnectionProperty twopcEnabled = new BooleanConnectionProperty(
        Constants.Property.TWOPC_ENABLED,
        "Whether to enable two-phased commit, for atomic distributed commits. See http://vitess.io/user-guide/twopc.html",
        false);
    private EnumConnectionProperty<Query.ExecuteOptions.IncludedFields> includedFields = new EnumConnectionProperty<>(
        Constants.Property.INCLUDED_FIELDS,
        "What fields to return from MySQL to the Driver. Limiting the fields returned can improve performance, but ALL is required for maximum JDBC API support",
        Constants.DEFAULT_INCLUDED_FIELDS);
    private EnumConnectionProperty<Topodata.TabletType> tabletType = new EnumConnectionProperty<>(
        Constants.Property.TABLET_TYPE,
        "Tablet Type to which Vitess will connect(master, replica, rdonly)",
        Constants.DEFAULT_TABLET_TYPE);
    private BooleanConnectionProperty skipV3 = new BooleanConnectionProperty(
        Constants.Property.SKIP_V3,
        "If enabled, the JDBC will skip the V3 API of VTGate, and instead run ExecuteShards using the old API. This means all queries will hit all shards for a keyspace, and queries will not be parsed",
        false);
    private ListConnectionProperty<String> skipV3Shards = new ListConnectionProperty<>(
        Constants.Property.SKIP_V3_SHARDS,
        "If skipV3 is enabled, use this configuration to specify which shards queries will be executed against as a comma-separated list of integers. I.E. 0,1",
        Collections.singletonList("0"),
        null
    );

    // TLS-related configs
    private BooleanConnectionProperty useSSL = new BooleanConnectionProperty(
        Constants.Property.USE_SSL,
        "Whether this connection should use transport-layer security",
        false);
    private StringConnectionProperty keyStore = new StringConnectionProperty(
        Constants.Property.KEYSTORE,
        "The Java .JKS keystore file to use when TLS is enabled",
        null,
        null);
    private StringConnectionProperty keyStorePassword = new StringConnectionProperty(
        Constants.Property.KEYSTORE_PASSWORD,
        "The password protecting the keystore file (if a password is set)",
        null,
        null);
    private StringConnectionProperty keyAlias = new StringConnectionProperty(
        Constants.Property.KEY_ALIAS,
        "Alias under which the private key is stored in the keystore file (if not specified, then the "
            + "first valid `PrivateKeyEntry` will be used)",
        null,
        null);
    private StringConnectionProperty keyPassword = new StringConnectionProperty(
        Constants.Property.KEY_PASSWORD,
        "The additional password protecting the private key entry within the keystore file (if not "
                + "specified, then the logic will fallback to the keystore password and then to no password at all)",
        null,
        null);
    private StringConnectionProperty trustStore = new StringConnectionProperty(
        Constants.Property.TRUSTSTORE,
        "The Java .JKS truststore file to use when TLS is enabled",
        null,
        null);
    private StringConnectionProperty trustStorePassword = new StringConnectionProperty(
        Constants.Property.TRUSTSTORE_PASSWORD,
        "The password protecting the truststore file (if a password is set)",
        null,
        null);
    private StringConnectionProperty trustAlias = new StringConnectionProperty(
        Constants.Property.TRUST_ALIAS,
        "Alias under which the certficate chain is stored in the truststore file (if not specified, then "
                + "the first valid `X509Certificate` will be used)",
        null,
        null);

    // Caching of some hot properties to avoid casting over and over
    private Topodata.TabletType tabletTypeCache;
    private Query.ExecuteOptions.IncludedFields includedFieldsCache;
    private boolean includeAllFieldsCache = true;
    private boolean twopcEnabledCache = false;
    private boolean simpleExecuteTypeCache = true;
    private String characterEncodingAsString = null;

    void initializeProperties(Properties props) throws SQLException {
        Properties propsCopy = (Properties) props.clone();
        for (Field propertyField : PROPERTY_LIST) {
            try {
                ConnectionProperty propToSet = (ConnectionProperty) propertyField.get(this);
                propToSet.initializeFrom(propsCopy);
            } catch (IllegalAccessException iae) {
                throw new SQLException("Unable to initialize driver properties due to " + iae.toString());
            }
        }
        postInitialization();
        checkConfiguredEncodingSupport();
    }

    private void postInitialization() {
        this.tabletTypeCache = this.tabletType.getValueAsEnum();
        this.includedFieldsCache = this.includedFields.getValueAsEnum();
        this.includeAllFieldsCache = this.includedFieldsCache == Query.ExecuteOptions.IncludedFields.ALL;
        this.twopcEnabledCache = this.twopcEnabled.getValueAsBoolean();
        this.simpleExecuteTypeCache = this.executeType.getValueAsEnum() == Constants.QueryExecuteType.SIMPLE;
        this.characterEncodingAsString = this.characterEncoding.getValueAsString();
    }

    /**
     * Attempt to use the encoding, and bail out if it can't be used
     * @throws SQLException if exception occurs while attempting to use the encoding
     */
    private void checkConfiguredEncodingSupport() throws SQLException {
        if (characterEncodingAsString != null) {
            try {
                String testString = "abc";
                StringUtils.getBytes(testString, characterEncodingAsString);
            } catch (UnsupportedEncodingException UE) {
                throw new SQLException("Unsupported character encoding: " + characterEncodingAsString);
            }
        }
    }

    static DriverPropertyInfo[] exposeAsDriverPropertyInfo(Properties info, int slotsToReserve) throws SQLException {
        return new ConnectionProperties().exposeAsDriverPropertyInfoInternal(info, slotsToReserve);
    }

    private DriverPropertyInfo[] exposeAsDriverPropertyInfoInternal(Properties info, int slotsToReserve) throws SQLException {
        initializeProperties(info);
        int numProperties = PROPERTY_LIST.size();
        int listSize = numProperties + slotsToReserve;
        DriverPropertyInfo[] driverProperties = new DriverPropertyInfo[listSize];

        for (int i = slotsToReserve; i < listSize; i++) {
            java.lang.reflect.Field propertyField = PROPERTY_LIST.get(i - slotsToReserve);
            try {
                ConnectionProperty propToExpose = (ConnectionProperty) propertyField.get(this);
                if (info != null) {
                    propToExpose.initializeFrom(info);
                }
                driverProperties[i] = propToExpose.getAsDriverPropertyInfo();
            } catch (IllegalAccessException iae) {
                throw new SQLException("Internal properties failure", iae);
            }
        }
        return driverProperties;
    }

    public boolean getBlobsAreStrings() {
        return blobsAreStrings.getValueAsBoolean();
    }

    public void setBlobsAreStrings(boolean blobsAreStrings) {
        this.blobsAreStrings.setValue(blobsAreStrings);
    }

    public boolean getUseBlobToStoreUTF8OutsideBMP() {
        return useBlobToStoreUTF8OutsideBMP.getValueAsBoolean();
    }

    public void setUseBlobToStoreUTF8OutsideBMP(boolean useBlobToStoreUTF8OutsideBMP) {
        this.useBlobToStoreUTF8OutsideBMP.setValue(useBlobToStoreUTF8OutsideBMP);
    }

    public boolean getTinyInt1isBit() {
        return tinyInt1isBit.getValueAsBoolean();
    }

    public void setTinyInt1isBit(boolean tinyInt1isBit) {
        this.tinyInt1isBit.setValue(tinyInt1isBit);
    }

    public boolean getFunctionsNeverReturnBlobs() {
        return functionsNeverReturnBlobs.getValueAsBoolean();
    }

    public void setFunctionsNeverReturnBlobs(boolean functionsNeverReturnBlobs) {
        this.functionsNeverReturnBlobs.setValue(functionsNeverReturnBlobs);
    }

    public String getUtf8OutsideBmpIncludedColumnNamePattern() {
        return utf8OutsideBmpIncludedColumnNamePattern.getValueAsString();
    }

    public void setUtf8OutsideBmpIncludedColumnNamePattern(String pattern) {
        this.utf8OutsideBmpIncludedColumnNamePattern.setValue(pattern);
    }

    public String getUtf8OutsideBmpExcludedColumnNamePattern() {
        return utf8OutsideBmpExcludedColumnNamePattern.getValueAsString();
    }

    public void setUtf8OutsideBmpExcludedColumnNamePattern(String pattern) {
        this.utf8OutsideBmpExcludedColumnNamePattern.setValue(pattern);
    }

    public boolean getYearIsDateType() {
        return yearIsDateType.getValueAsBoolean();
    }

    public void setYearIsDateType(boolean yearIsDateType) {
        this.yearIsDateType.setValue(yearIsDateType);
    }

    public String getEncoding() {
        return characterEncodingAsString;
    }

    public void setEncoding(String encoding) {
        this.characterEncoding.setValue(encoding);
        this.characterEncodingAsString = this.characterEncoding.getValueAsString();
    }

    public Query.ExecuteOptions.IncludedFields getIncludedFields() {
        return this.includedFieldsCache;
    }

    public boolean isIncludeAllFields() {
        return this.includeAllFieldsCache;
    }

    public void setIncludedFields(Query.ExecuteOptions.IncludedFields includedFields) {
        this.includedFields.setValue(includedFields);
        this.includedFieldsCache = includedFields;
        this.includeAllFieldsCache = includedFields == Query.ExecuteOptions.IncludedFields.ALL;
    }

    public boolean getTwopcEnabled() {
        return twopcEnabledCache;
    }

    public void setTwopcEnabled(boolean twopcEnabled) {
        this.twopcEnabled.setValue(twopcEnabled);
        this.twopcEnabledCache = this.twopcEnabled.getValueAsBoolean();
    }

    public Constants.QueryExecuteType getExecuteType() {
        return executeType.getValueAsEnum();
    }

    public boolean isSimpleExecute() {
        return simpleExecuteTypeCache;
    }

    public void setExecuteType(Constants.QueryExecuteType executeType) {
        this.executeType.setValue(executeType);
        this.simpleExecuteTypeCache = this.executeType.getValueAsEnum() == Constants.QueryExecuteType.SIMPLE;
    }

    public Topodata.TabletType getTabletType() {
        return tabletTypeCache;
    }

    public void setTabletType(Topodata.TabletType tabletType) {
        this.tabletType.setValue(tabletType);
        this.tabletTypeCache = this.tabletType.getValueAsEnum();
    }

    public boolean getUseSSL() {
        return useSSL.getValueAsBoolean();
    }

    public String getKeyStore() {
        return keyStore.getValueAsString();
    }

    public String getKeyStorePassword() {
        return keyStorePassword.getValueAsString();
    }

    public String getKeyAlias() {
        return keyAlias.getValueAsString();
    }

    public String getKeyPassword() {
        return keyPassword.getValueAsString();
    }

    public String getTrustStore() {
        return trustStore.getValueAsString();
    }

    public String getTrustStorePassword() {
        return trustStorePassword.getValueAsString();
    }

    public String getTrustAlias() {
        return trustAlias.getValueAsString();
    }

    public boolean getSkipV3() {
        return this.skipV3.getValueAsBoolean();
    }

    public void setSkipV3(boolean skipV3) {
        this.skipV3.setValue(skipV3);
    }

    public List<String> getSkipV3Shards() {
        return this.skipV3Shards.getValueAsList();
    }

    public void setSkipV3Shards(List<String> shards) {
        this.skipV3Shards.setValue(shards);
    }

    abstract static class ConnectionProperty {

        private final String name;
        private final boolean required = false;
        private final String description;
        final Object defaultValue;
        Object valueAsObject;

        private ConnectionProperty(String name, String description, Object defaultValue) {
            this.name = name;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        void initializeFrom(Properties extractFrom) {
            String extractedValue = extractFrom.getProperty(getPropertyName());
            String[] allowable = getAllowableValues();
            if (allowable != null && extractedValue != null) {
                boolean found = false;
                for (String value : allowable) {
                    found |= value.equalsIgnoreCase(extractedValue);
                }
                if (!found) {
                    throw new IllegalArgumentException("Property '" + name + "' Value '" + extractedValue + "' not in the list of allowable values: " + Arrays.toString(allowable));
                }
            }
            extractFrom.remove(getPropertyName());
            initializeFrom(extractedValue);
        }

        abstract void initializeFrom(String extractedValue);

        abstract String[] getAllowableValues();

        public String getPropertyName() {
            return name;
        }

        DriverPropertyInfo getAsDriverPropertyInfo() {
            DriverPropertyInfo dpi = new DriverPropertyInfo(this.name, null);
            dpi.choices = getAllowableValues();
            dpi.value = (this.valueAsObject != null) ? this.valueAsObject.toString() : null;
            dpi.required = this.required;
            dpi.description = this.description;
            return dpi;
        }
    }

    private static class BooleanConnectionProperty extends ConnectionProperty {

        private BooleanConnectionProperty(String name, String description, Boolean defaultValue) {
            super(name, description, defaultValue);
        }

        @Override
        void initializeFrom(String extractedValue) {
            if (extractedValue != null) {
                setValue(extractedValue.equalsIgnoreCase("TRUE") || extractedValue.equalsIgnoreCase("YES"));
            } else {
                this.valueAsObject = this.defaultValue;
            }
        }

        public void setValue(boolean value) {
            this.valueAsObject = value;
        }

        @Override
        String[] getAllowableValues() {
            return new String[]{Boolean.toString(true), Boolean.toString(false), "yes", "no"};
        }

        boolean getValueAsBoolean() {
            return (boolean) valueAsObject;
        }
    }

    private static class StringConnectionProperty extends ConnectionProperty {

        private final String[] allowableValues;

        private StringConnectionProperty(String name, String description, String defaultValue, String[] allowableValuesToSet) {
            super(name, description, defaultValue);
            allowableValues = allowableValuesToSet;
        }

        @Override
        void initializeFrom(String extractedValue) {
            if (extractedValue != null) {
                setValue(extractedValue);
            } else {
                this.valueAsObject = this.defaultValue;
            }
        }

        @Override
        String[] getAllowableValues() {
            return allowableValues;
        }

        public void setValue(String value) {
            this.valueAsObject = value;
        }

        String getValueAsString() {
            return valueAsObject == null ? null : valueAsObject.toString();
        }
    }

    private static class EnumConnectionProperty<T extends Enum<T>> extends ConnectionProperty {

        private final Class<T> clazz;

        private EnumConnectionProperty(String name, String description, Enum<T> defaultValue) {
            super(name, description, defaultValue);
            this.clazz = defaultValue.getDeclaringClass();
            if (!clazz.isEnum()) {
                throw new IllegalArgumentException("EnumConnectionProperty types should be enums");
            }
        }

        @Override
        void initializeFrom(String extractedValue) {
            if (extractedValue != null) {
                setValue(Enum.valueOf(clazz, extractedValue.toUpperCase()));
            } else {
                this.valueAsObject = this.defaultValue;
            }
        }

        public void setValue(T value) {
            this.valueAsObject = value;
        }

        @Override
        String[] getAllowableValues() {
            T[] enumConstants = clazz.getEnumConstants();
            String[] allowed = new String[enumConstants.length];
            for (int i = 0; i < enumConstants.length; i++) {
                allowed[i] = enumConstants[i].toString();
            }
            return allowed;
        }

        T getValueAsEnum() {
            return (T) valueAsObject;
        }
    }

    private static class ListConnectionProperty<T> extends ConnectionProperty {

        private final Function<String, T> valueConverter;

        private ListConnectionProperty(String name, String description, List<T> defaultValue, Function<String, T> valueConverter) {
            super(name, description, defaultValue);
            this.valueConverter = valueConverter;
        }

        @Override
        void initializeFrom(String extractedValue) {
            if (extractedValue == null) {
                this.valueAsObject = this.defaultValue;
                return;
            }

            String[] parts = extractedValue.split(",");
            List<T> list = new ArrayList<>(parts.length);
            for (int i  = 0; i < parts.length; i++) {
                list.add(i, valueConverter == null ? (T) parts[i] : valueConverter.apply(parts[i]));
            }

            setValue(list);
        }

        public void setValue(List<T> value) {
            this.valueAsObject = value;
        }

        @Override
        String[] getAllowableValues() {
            return new String[0];
        }

        List<T> getValueAsList() {
            return (List<T>) valueAsObject;
        }
    }
}
