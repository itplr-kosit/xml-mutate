package de.kosit.xmlmutate.runner;

import de.init.kosit.commons.util.CommonException;
import de.init.kosit.commons.util.NamedError;

/**
 * @author Andreas Penski
 */
public class MutationException extends CommonException {

    public MutationException(final NamedError error) {
        super(error);
    }

    public MutationException(final NamedError error, final Exception e) {
        super(error, e);
    }

    public MutationException(final NamedError error, final Object... params) {
        super(error, params);
    }
}
