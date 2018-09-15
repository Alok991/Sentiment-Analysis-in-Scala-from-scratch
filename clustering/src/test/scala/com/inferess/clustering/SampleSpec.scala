/*
 *  Copyright (c) 2017. Inferess and/or its affiliates. All rights reserved.
 *  Inferess PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.inferess.clustering

import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

/**
  * Specifications for sentence structure learning utilities
  */
class SslUtilsSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {
  private val Numbers = Table(
  ("numbers", "expected"),
      (1,         2),
      (4,         5),
      (6,         7)
  )

  property("addition is simple") {
    forAll(Numbers) {
      (numbers: Int, expected: Int) => {
        numbers+1 should be (expected)
      }
    }
  }
}



