package com.scilari.systematic_alias_sampling.util

import com.scilari.systematic_alias_sampling.models.Random

import scala.math.Fractional
import scala.reflect.ClassTag
import scala.{specialized => sp}

/**
  * Basic helper methods
  * Created by iv on 21.1.2014.
 */
object Helpers {
  def distributionApproximation(distribution: Double => Double,
                               minX: Double,
                               maxX: Double,
                               binCount: Int)
                               (implicit num: Fractional[Double], tag: ClassTag[Double]): (Array[Double], Array[Double]) = {
    require(minX < maxX, "minX is not less than maxX.")
    val points = Helpers.linspace(minX, maxX, binCount)
    val pmf = Helpers.normalizeSum(points map distribution, 1.0)
    (pmf, points)
  }

  class NormalDistribution(mean: Double, dev: Double){
    def density(x: Double): Double = 1.0/(math.sqrt(2*math.Pi)*dev)*math.exp(-(x - mean)*(x - mean)/(2.0*dev*dev))
  }

  val random: Random = Random.default

  def cumsum[@sp(Float, Double) T](a: Array[T])(implicit num: Fractional[T], tag: ClassTag[T]): Array[T] = {
    import num._
    val cs = new Array[T](a.length)
    cs(0) = a(0)
    for (i <- 1 until cs.length) cs(i) = cs(i - 1) + a(i)
    cs
  }

  def normalizeSum[@sp(Float, Double) T](a: Array[T], to: T)(implicit num: Fractional[T], tag: ClassTag[T]): Array[T] = {
    import num._
    val invSum = to / a.sum
    a map {
      _ * invSum
    }
  }

  def linspace[@sp(Float, Double) T](minX: T, maxX: T, pointCount: Int)
                                      (implicit num: Fractional[T], tag: ClassTag[T]): Array[T] = {
    import num._
    val step = (maxX - minX) / fromInt(pointCount - 1)
    val a = new Array[T](pointCount)
    for (i <- 0 until pointCount) a(i) = minX + fromInt(i) * step
    a
  }

  def pointToIndex[T](x: T, minX: T, maxX: T, binCount: Int)(implicit num: Fractional[T], tag: ClassTag[T]): Int = {
    import num._
    val ix = ((x - minX) / (maxX - minX) * fromInt(binCount)).toInt
    0 max ix min (binCount - 1)
  }

  def buildHistogram[T](samples: Array[T], minX: T, maxX: T, binCount: Int)(implicit num: Fractional[T], tag: ClassTag[T]): Array[T] = {
    import num._
    val indexes = samples map {
      pointToIndex(_, minX, maxX, binCount)
    }
    val histogram = new Array[T](binCount)
    for (i <- indexes) {
      histogram(i) += one
    }
    histogram
  }

  def sampleNormal(dev: Double): Double = {
    dev * sampleNormal
  }

  def sampleNormal(): Double = {
    var sum = 0.0
    for (i <- 0 until 12) sum += random.nextDouble()
    sum - 6.0
  }

  def sampleNormal(dev: Double = 1.0, k: Int): Array[Double] = {
    val a = new Array[Double](k)
    if (dev != 1.0){
      for (i <- 0 until k) a(i) = sampleNormal(dev)
    } else {
      for (i <- 0 until k) a(i) = sampleNormal()
    }
    a
  }

  class ArrayShuffler[@sp(Float, Double) T](val n: Int)(implicit tag: ClassTag[T]) {
    val indices: Array[Int] = scala.util.Random.shuffle((0 until n).toList).toArray

    def shuffle(array: Array[T]): Array[T] = {
      val shuffled = new Array[T](n)
      val offset = random.nextInt(n)
      for (i <- array.indices) {
        val ix = i + offset
        if (ix < n){
          shuffled(i) = array(indices(ix))
        } else {
          shuffled(i) = array(indices(ix - n))
        }


      }
      shuffled
    }
  }
}
