/**
 *Licensed to the Apache Software Foundation (ASF) under one
 *or more contributor license agreements.  See the NOTICE file
 *distributed with this work for additional information
 *regarding copyright ownership.  The ASF licenses this file
 *to you under the Apache License, Version 2.0 (the"
 *License"); you may not use this file except in compliance
 *with the License.  You may obtain a copy of the License at
 *
  * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */

package com.eyllo.paprika.entity.elements;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.util.Utf8;
import org.apache.avro.ipc.AvroRemoteException;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.FixedSize;
import org.apache.avro.specific.SpecificExceptionBase;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificFixed;
import org.apache.gora.persistency.StateManager;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.persistency.impl.StateManagerImpl;
import org.apache.gora.persistency.StatefulHashMap;
import org.apache.gora.persistency.ListGenericArray;
import org.bingmaps.rest.models.Confidence;

@SuppressWarnings("all")
public class PersistentPoint extends PersistentBase {
  public static final Schema _SCHEMA = Schema.parse("{\"type\":\"record\",\"name\":\"PersistentPoint\",\"namespace\":\"com.eyllo.paprika.entity.generated\",\"fields\":[{\"name\":\"accuracy\",\"type\":\"double\"},{\"name\":\"address\",\"type\":\"string\"},{\"name\":\"coordinates\",\"type\":{\"type\":\"array\",\"items\":\"double\"}}]}");
  public static enum Field {
    ACCURACY(0,"accuracy"),
    ADDRESS(1,"address"),
    COORDINATES(2,"coordinates"),
    ;
    private int index;
    private String name;
    Field(int index, String name) {this.index=index;this.name=name;}
    public int getIndex() {return index;}
    public String getName() {return name;}
    public String toString() {return name;}
  };
  public static final String[] _ALL_FIELDS = {"accuracy","address","coordinates",};
  static {
    PersistentBase.registerFields(PersistentPoint.class, _ALL_FIELDS);
  }
  private double accuracy;
  private Utf8 address;
  private GenericArray<Double> coordinates;
  public PersistentPoint() {
    this(new StateManagerImpl());
  }
  public PersistentPoint(StateManager stateManager) {
    super(stateManager);
    coordinates = new ListGenericArray<Double>(getSchema().getField("coordinates").schema());
  }
  public PersistentPoint newInstance(StateManager stateManager) {
    return new PersistentPoint(stateManager);
  }
  public Schema getSchema() { return _SCHEMA; }
  public Object get(int _field) {
    switch (_field) {
    case 0: return accuracy;
    case 1: return address;
    case 2: return coordinates;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int _field, Object _value) {
    if(isFieldEqual(_field, _value)) return;
    getStateManager().setDirty(this, _field);
    switch (_field) {
    case 0:accuracy = (Double)_value; break;
    case 1:address = (Utf8)_value; break;
    case 2:coordinates = (GenericArray<Double>)_value; break;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  public double getAccuracy() {
    return (Double) get(0);
  }
  public void setAccuracy(double value) {
    put(0, value);
  }
  public Utf8 getAddress() {
    return (Utf8) get(1);
  }
  public void setAddress(Utf8 value) {
    put(1, value);
  }
  public GenericArray<Double> getCoordinates() {
    return (GenericArray<Double>) get(2);
  }
  public void addToCoordinates(double element) {
    getStateManager().setDirty(this, 2);
    coordinates.add(element);
  }
  /**
   * Sets Accuracy based on geocoder's confidence level
   * @param pConfidence
   */
  public void setAccuracyFromGeocoder(int pConfidence){
      switch (pConfidence ){
          case Confidence.High: this.setAccuracy(EylloLocation.INITIAL_ACC_HIGH);break;
          case Confidence.Medium: this.setAccuracy(EylloLocation.INITIAL_ACC_MEDIUM);break;
          case Confidence.Low: this.setAccuracy(EylloLocation.INITIAL_ACC_LOW); break;
          case Confidence.Unknown: this.setAccuracy(EylloLocation.INITIAL_ACC_LOW); break;
      }
  }
}
