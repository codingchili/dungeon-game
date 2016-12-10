package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a request has error validation.
 */
public class RequestValidationException extends CoreException {

    public RequestValidationException() {
        super(CoreStrings.ERROR_VALIDATION_FAILURE);
    }
}