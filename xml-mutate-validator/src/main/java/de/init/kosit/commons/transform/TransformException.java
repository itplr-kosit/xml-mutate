package de.init.kosit.commons.transform;

import java.util.Collection;
import java.util.List;

import de.init.kosit.commons.util.BusinessError;
import de.init.kosit.commons.util.CommonException;
import de.init.kosit.commons.util.NamedError;

/**
 * @author Andreas Penski
 */
public class TransformException extends CommonException {

    public TransformException(final NamedError error) {
        super(error);
    }

    public TransformException(final NamedError error, final Exception e) {
        super(error, e);
    }

    public TransformException(final NamedError error, final Object... params) {
        super(error, params);
    }

    public TransformException(final BusinessError error) {
        super(error);
    }

    public TransformException(final BusinessError error, final Exception cause) {
        super(error, cause);
    }

    public TransformException(final Collection<BusinessError> errors) {
        super(errors);
    }

    public TransformException(final Collection<BusinessError> errors, final Exception cause) {
        super(errors, cause);
    }

    public TransformException(final List<BusinessError> errors, final Exception cause) {
        super(errors, cause);
    }
}
