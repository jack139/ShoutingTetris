/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.40
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.jack139.pocketsphinx;

public class Nbest {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Nbest(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Nbest obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pocketsphinxJNI.delete_Nbest(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setNbest(SWIGTYPE_p_ps_nbest_t value) {
    pocketsphinxJNI.Nbest_nbest_set(swigCPtr, this, SWIGTYPE_p_ps_nbest_t.getCPtr(value));
  }

  public SWIGTYPE_p_ps_nbest_t getNbest() {
    long cPtr = pocketsphinxJNI.Nbest_nbest_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_ps_nbest_t(cPtr, false);
  }

  public Nbest(Decoder d) {
    this(pocketsphinxJNI.new_Nbest(Decoder.getCPtr(d), d), true);
  }

  public boolean next() {
    return pocketsphinxJNI.Nbest_next(swigCPtr, this);
  }

  public Hypothesis hyp() {
    long cPtr = pocketsphinxJNI.Nbest_hyp(swigCPtr, this);
    return (cPtr == 0) ? null : new Hypothesis(cPtr, false);
  }

}
