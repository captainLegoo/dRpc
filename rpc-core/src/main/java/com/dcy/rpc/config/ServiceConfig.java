package com.dcy.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kyle
 * @date 2024/02/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfig<T> {
    // interface
    private Class<?> interfaceRef;
    // implement
    private Object implRef;
}

