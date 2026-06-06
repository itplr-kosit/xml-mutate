package de.init.kosit.commons.transform;

import net.sf.saxon.s9api.Destination;

/**
 * @author Andreas Penski
 */
interface Convert<T> {

    Destination createDestination();

    T getResult();

}
