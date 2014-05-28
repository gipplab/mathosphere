/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package cc.clabs.stratosphere.mlp.contracts;

import eu.stratosphere.api.java.record.functions.ReduceFunction;
import eu.stratosphere.types.DoubleValue;
import eu.stratosphere.types.Record;
import eu.stratosphere.util.Collector;

import java.util.Iterator;

/**
 *
 * @author rob
 */
public class AccuracyAggregator extends ReduceFunction {
        
    private final Record target = new Record();

    @Override
    public void reduce( Iterator<Record> iterator, Collector<Record> collector ) throws Exception {
        Record record;
        
        Double level = 0.8d;
        Double accuracy = 0.9d;
        
        while ( iterator.hasNext() ) {
            record = iterator.next();
            
            // emit
            target.clear();
            target.setField( 0, new DoubleValue( level ) );
            target.setField( 1, new DoubleValue( accuracy ) );
            collector.collect( target );
        }
    }

}