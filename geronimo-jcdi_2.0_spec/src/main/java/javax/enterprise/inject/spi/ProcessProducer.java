/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package javax.enterprise.inject.spi;

import javax.enterprise.inject.spi.configurator.ProducerConfigurator;

/**
 * Container fires this event for each
 * producer field/method including resources.
 * 
 * @version $Rev$ $Date$
 *
 * @param <BEANCLASS> bean class info
 * @param <RETURNTYPE> producer return type
 */
public interface ProcessProducer<BEANCLASS, RETURNTYPE>
{
    /**
     * Returns annotated member.
     * 
     * @return annotated member
     */
    AnnotatedMember<BEANCLASS> getAnnotatedMember();
    
    /**
     * Returns producer instance.
     * 
     * @return producer instance
     */
    Producer<RETURNTYPE> getProducer();
    
    /**
     * Replaces producer instance.
     * 
     * @param producer new producer
     */
    void setProducer(Producer<RETURNTYPE> producer);

    /**
     * Adding definition error. Container aborts processing.
     * 
     * @param t throwable
     */
    void addDefinitionError(Throwable t);

    /**
     * This method can be used to tweak the underlying Producer currently processed.
     *
     * @return a ProducerConfigurator, initialised with the Producer of this event.
     */
    ProducerConfigurator<RETURNTYPE> configureProducer();
    

}