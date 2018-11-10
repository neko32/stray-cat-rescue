package org.catrescue

import com.typesafe.config.ConfigFactory

trait Config {

  def appConfig = {
    ConfigFactory.load("main")
  }

}
