package com.shen.xi.android.tut


import android.os.AsyncTask
import android.util.Log

import com.google.common.collect.Collections2
import com.google.common.collect.Lists
import com.google.inject.Inject
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.shen.xi.android.tut.event.RefreshStatusCompleteEvent
import com.shen.xi.android.tut.event.RefreshWeiboEvent
import com.shen.xi.android.tut.util.Util
import com.shen.xi.android.tut.weibo.WeiboClient
import com.shen.xi.android.tut.weibo.WeiboStatus
import com.shen.xi.android.tut.weibo.WeiboTimeline
import com.shen.xi.android.tut.weibo.WeiboTimelineException

import java.util.{Collection, List}
import java.lang.Runnable

import scala.concurrent.{future, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.collection.JavaConversions._


class WeiboClientManager @Inject() (bus: Bus, client: WeiboClient) {

  private val TAG = classOf[WeiboClientManager].getName
  private val mClient = client
  private val mBus = bus

  mBus.register(this)

  @Subscribe
  def refreshStatus_scala(event: RefreshWeiboEvent): Unit = {
    var completeEvent = new RefreshStatusCompleteEvent()

    future {

      val timeline = mClient.getHomeTimeline(event.sinceId)
      if (timeline != null && timeline.statuses.size > 0) {
        bufferAsJavaList(timeline.statuses.filter(_.getImageUrl != null))
      } else null

    } onComplete {

      case Success(statuses) => {
        event.activity.runOnUiThread(new Runnable() {
          def run() {
            mBus.post(completeEvent.setStatus(statuses))
          }
        })
      }

      case Failure(e) => {
        e.printStackTrace
        event.activity.runOnUiThread(new Runnable() {
          def run() {
            mBus.post(completeEvent.setStatus(null))
          }
        })
      }

    }
  }

  @throws[Throwable]
  override protected def finalize() = {
    mBus.unregister(this)

    super.finalize()
  }

}
