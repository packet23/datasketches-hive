package com.yahoo.sketches.hive.quantiles;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.BytesWritable;

import com.yahoo.sketches.quantiles.QuantilesSketch;

abstract class QuantilesEvaluator extends GenericUDAFEvaluator {

  protected PrimitiveObjectInspector inputObjectInspector;

  @Override
  public ObjectInspector init(final Mode mode, final ObjectInspector[] parameters) throws HiveException {
    super.init(mode, parameters);
    inputObjectInspector = (PrimitiveObjectInspector) parameters[0];
    return PrimitiveObjectInspectorFactory.getPrimitiveWritableObjectInspector(PrimitiveCategory.BINARY);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void reset(final AggregationBuffer buf) throws HiveException {
    final QuantilesUnionState state = (QuantilesUnionState) buf;
    state.reset();
  }

  @SuppressWarnings("deprecation")
  @Override
  public Object terminatePartial(final AggregationBuffer buf) throws HiveException {
    return terminate(buf);
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void merge(final AggregationBuffer buf, Object data) throws HiveException {
    if (data == null) return;
    final QuantilesUnionState state = (QuantilesUnionState) buf;
    final BytesWritable serializedSketch = (BytesWritable) inputObjectInspector.getPrimitiveWritableObject(data);
    state.update(serializedSketch.getBytes());
  }

  @SuppressWarnings("deprecation")
  @Override
  public Object terminate(final AggregationBuffer buf) throws HiveException {
    final QuantilesUnionState state = (QuantilesUnionState) buf;
    final QuantilesSketch resultSketch = state.getResult();
    if (resultSketch == null) return null;
    return new BytesWritable(resultSketch.toByteArray());
  }

  @SuppressWarnings("deprecation")
  @Override
  public AggregationBuffer getNewAggregationBuffer() throws HiveException {
    return new QuantilesUnionState();
  }

}
