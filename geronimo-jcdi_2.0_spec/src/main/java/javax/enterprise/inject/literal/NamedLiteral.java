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
package javax.enterprise.inject.literal;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

/**
 *
 * @since 2.0
 */
public final class NamedLiteral extends AnnotationLiteral<Named> implements Named
{
    public static final Named INSTANCE = of("");

    private static final long serialVersionUID = 1L;

    private final String value;

    public static NamedLiteral of(String name)
    {
        return new NamedLiteral(name);
    }

    private NamedLiteral(String value)
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }
}
