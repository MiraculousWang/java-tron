/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.common.storage.leveldb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.config.args.Args;

@Slf4j
public class LevelDbDataSourceImplTest {

  private static final String dbPath = "output-levelDb-test";
  LevelDbDataSourceImpl dataSourceTest;

  @Before
  public void initDb() {
    Args.setParam(new String[]{"--output-directory", dbPath},
        "config-junit.conf");
    dataSourceTest = new LevelDbDataSourceImpl(dbPath + File.separator,
        "test_levelDb");
  }

  /**
   * Release resources.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  @Test
  public void testPutGet() {
    dataSourceTest.resetDb();
    String key1 = "2c0937534dd1b3832d05d865e8e6f2bf23218300b33a992740d45ccab7d4f519";
    byte[] key = key1.getBytes();
    dataSourceTest.initDB();
    String value1 = "50000";
    byte[] value = value1.getBytes();

    dataSourceTest.putData(key, value);

    assertNotNull(dataSourceTest.getData(key));
    assertEquals(1, dataSourceTest.allKeys().size());
    assertEquals("50000", ByteArray.toStr(dataSourceTest.getData(key1.getBytes())));

  }

  @Test
  public void testReset() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_reset");
    dataSource.resetDb();
    assertEquals(0, dataSource.allKeys().size());
    //dataSource.closeDB();
  }

  @Test
  public void testupdateByBatchInner() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_updateByBatch");
    dataSource.initDB();
    dataSource.resetDb();
    String key1 = "431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757350";
    String value1 = "50000";
    String key2 = "431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757351";
    String value2 = "10000";

    Map<byte[], byte[]> rows = new HashMap<>();
    rows.put(key1.getBytes(), value1.getBytes());
    rows.put(key2.getBytes(), value2.getBytes());

    dataSource.updateByBatch(rows);

    assertEquals("50000", ByteArray.toStr(dataSource.getData(key1.getBytes())));
    assertEquals("10000", ByteArray.toStr(dataSource.getData(key2.getBytes())));
    assertEquals(2, dataSource.allKeys().size());
  }

  @Test
  public void testdeleteData() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_delete");
    dataSource.initDB();
    String key1 = "431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757350";
    byte[] key = key1.getBytes();
    dataSource.deleteData(key);
    byte[] value = dataSource.getData(key);
    String s = ByteArray.toStr(value);
    assertNull(s);

  }

  @Test
  public void testallKeys() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_find_key");
    dataSource.initDB();
    dataSource.resetDb();

    String key1 = "431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757321";
    byte[] key = key1.getBytes();

    String value1 = "50000";
    byte[] value = value1.getBytes();

    dataSource.putData(key, value);
    String key3 = "431cd8c8d5abe5cb5944b0889b32482d85772fbb98987b10fbb7f17110757091";
    byte[] key2 = key3.getBytes();

    String value3 = "30000";
    byte[] value2 = value3.getBytes();

    dataSource.putData(key2, value2);
    assertEquals(2, dataSource.allKeys().size());
    dataSource.resetDb();
  }

  @Test(timeout = 1000)
  public void testLockReleased() {
    dataSourceTest.initDB();
    // normal close
    dataSourceTest.closeDB();
    // closing already closed db.
    dataSourceTest.closeDB();
    // closing again to make sure the lock is free. If not test will hang.
    dataSourceTest.closeDB();

    assertFalse("Database is still alive after closing.", dataSourceTest.isAlive());
  }

  @Test
  public void allKeysTest() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_allKeysTest_key");
    dataSource.initDB();
    dataSource.resetDb();

    byte[] key = "0000000987b10fbb7f17110757321".getBytes();
    byte[] value = "50000".getBytes();
    byte[] key2 = "000000431cd8c8d5a".getBytes();
    byte[] value2 = "30000".getBytes();

    dataSource.putData(key, value);
    dataSource.putData(key2, value2);
    dataSource.allKeys().forEach(keyOne -> {
      logger.info(ByteArray.toStr(keyOne));
    });
    assertEquals(2, dataSource.allKeys().size());
    dataSource.resetDb();
  }

  @Test
  public void seekTest() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_seek_key");
    dataSource.initDB();
    dataSource.resetDb();

    byte[] value1 = "10000".getBytes();
    byte[] value2 = "20000".getBytes();
    byte[] value3 = "30000".getBytes();
    byte[] value4 = "40000".getBytes();
    byte[] value5 = "50000".getBytes();
    byte[] value6 = "60000".getBytes();
    byte[] key1 = "00000001aa".getBytes();
    byte[] key2 = "00000002aa".getBytes();
    byte[] key3 = "00000003aa".getBytes();
    byte[] key4 = "00000004aa".getBytes();
    byte[] key5 = "00000005aa".getBytes();
    byte[] key6 = "00000006aa".getBytes();

    dataSource.putData(key1, value1);
    dataSource.putData(key6, value2);
    dataSource.putData(key2, value3);
    dataSource.putData(key5, value4);
    dataSource.putData(key3, value5);
    dataSource.putData(key4, value6);
    dataSource.resetDb();
  }

  @Test
  public void getSeekKeyLimitNext() {
    LevelDbDataSourceImpl dataSource = new LevelDbDataSourceImpl(
        Args.getInstance().getOutputDirectory(), "test_getSeekKeyLimitNext_key");
    dataSource.initDB();
    dataSource.resetDb();

    byte[] value1 = "10000".getBytes();
    byte[] value2 = "20000".getBytes();
    byte[] value3 = "30000".getBytes();
    byte[] value4 = "40000".getBytes();
    byte[] value5 = "50000".getBytes();
    byte[] value6 = "60000".getBytes();
    byte[] key1 = "00000001aa".getBytes();
    byte[] key2 = "00000002aa".getBytes();
    byte[] key3 = "00000003aa".getBytes();
    byte[] key4 = "00000004aa".getBytes();
    byte[] key5 = "00000005aa".getBytes();
    byte[] key6 = "00000006aa".getBytes();

    dataSource.putData(key1, value1);
    dataSource.putData(key6, value6);
    dataSource.putData(key2, value2);
    dataSource.putData(key5, value5);
    dataSource.putData(key3, value3);
    dataSource.putData(key4, value4);
    Set<byte[]> seekKeyLimitNext = dataSource.getSeekKeyLimitNext("0000000300".getBytes(), 2);
    seekKeyLimitNext.forEach(valeu -> {
      logger.info(ByteArray.toStr(valeu));
    });
    dataSource.resetDb();
  }

  @Test
  public void getSeekKeyLimitPrev() {
  }

  @Test
  public void getBySeekKeyNext() {
  }

  @Test
  public void getBySeekKeyPrev() {
  }
}