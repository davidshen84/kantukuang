package com.shen.xi.android.tut.event

import android.os.Bundle
import com.shen.xi.android.tut.{Unknown, ImageSource}

class SelectItemEvent {
  var extras: Bundle = null
  var source: ImageSource = Unknown
}
