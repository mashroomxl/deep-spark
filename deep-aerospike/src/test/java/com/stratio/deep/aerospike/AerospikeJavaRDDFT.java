/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.deep.aerospike;

import static org.testng.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.ScanCallback;
import com.aerospike.client.policy.ScanPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.IndexType;
import com.google.common.io.Resources;
import com.stratio.deep.core.extractor.ExtractorTest;

@Test(groups = { "AerospikeJavaRDDFT", "FunctionalTests" })
public class AerospikeJavaRDDFT {

    private static final Logger LOG = LoggerFactory.getLogger(AerospikeJavaRDDFT.class);

    public static AerospikeClient aerospike = null;

    public static final Integer PORT = 3000;

    public static final String HOST = "10.200.0.58";

    public static final String NAMESPACE_CELL = "aerospikecellextractor";

    public static final String NAMESPACE_ENTITY = "aerospikeentityextractor";

    public static final String SET_NAME = "input";

    public static final String SET_NAME_BOOK = "bookinput";

    public static final String DATA_SET_NAME = "divineComedy.json";

    @BeforeSuite
    public static void init() throws IOException, ParseException {
        aerospike = new AerospikeClient(HOST, PORT);
        deleteData();
        //dataSetImport();
    }

    @Test
    public void testRDD() {
        assertEquals(true, true, "Dummy test");
    }

    /**
     * Imports dataset.
     *
     * @throws java.io.IOException
     */
    private static void dataSetImport() throws IOException, ParseException {
        URL url = Resources.getResource(DATA_SET_NAME);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(url.getFile()));
        JSONObject jsonObject = (JSONObject) obj;

        String id = (String) jsonObject.get("id");
        JSONObject metadata = (JSONObject) jsonObject.get("metadata");
        JSONArray cantos = (JSONArray) jsonObject.get("cantos");

        Key key = new Key(NAMESPACE_ENTITY, SET_NAME, id);
        Bin binId = new Bin("id", id);
        Bin binMetadata = new Bin("metadata", metadata);
        Bin binCantos = new Bin("cantos", cantos);
        aerospike.put(null, key, binId, binMetadata, binCantos);
        aerospike.createIndex(null, NAMESPACE_ENTITY, SET_NAME, "id_idx", "id", IndexType.STRING);

        Key key2 = new Key(NAMESPACE_CELL, SET_NAME, 3);
        Bin bin_id = new Bin("_id", "3");
        Bin bin_number = new Bin("number", 3);
        Bin bin_text = new Bin("message", "new message test");
        aerospike.put(null, key2, bin_id, bin_number, bin_text);
        aerospike.createIndex(null, NAMESPACE_CELL, SET_NAME, "num_idx", "number", IndexType.NUMERIC);
        aerospike.createIndex(null, NAMESPACE_CELL, SET_NAME, "_id_idx", "_id", IndexType.STRING);
    }

    /**
     * Delete previously loaded data for starting with a fresh dataset.
     */
    private static void deleteData() {
        try {
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_CELL, SET_NAME, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_CELL, SET_NAME_BOOK, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_ENTITY, SET_NAME, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_ENTITY, SET_NAME_BOOK, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_ENTITY, ExtractorTest.FOOTBALL_TEAM_INPUT, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
            aerospike.scanAll(new ScanPolicy(), NAMESPACE_ENTITY, ExtractorTest.FOOTBALL_PLAYER_INPUT, new ScanCallback() {
                @Override
                public void scanCallback(Key key, Record record) throws AerospikeException {
                    aerospike.delete(new WritePolicy(), key);
                }
            }, new String[] { });
        } catch (AerospikeException e) {
            LOG.error("Error while deleting data", e);
        }
    }

    @AfterSuite
    public static void cleanup() {
        try {
            aerospike.close();
        } catch (Exception e) {
            LOG.error("Error while closing Aerospike client connection", e);
        }

    }

}
