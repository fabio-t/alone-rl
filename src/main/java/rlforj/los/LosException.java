/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.los;

/**
 * Exception thrown by LOS algorithms.
 *
 * @author sdatta
 */
public class LosException extends RuntimeException
{
    private static final long serialVersionUID = 8210411767466028759L;

    public LosException(final String msg)
    {
        super(msg);
    }
}
