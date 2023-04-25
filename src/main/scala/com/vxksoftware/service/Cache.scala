package com.vxksoftware.service
import zio.*

trait Cache[K, V]:
  def get(key: K): Task[Option[V]]
  def set(key: K, value: V): Task[Unit]

object Cache {
  case class MemCache[K, V](mapRef: Ref[Map[K, V]]) extends Cache[K, V] {
    def get(key: K): Task[Option[V]] =
      for map <- mapRef.get
      yield map.get(key)

    def set(key: K, value: V): Task[Unit] =
      for _ <- mapRef.update(_.updated(key, value))
      yield ()
  }

  def make[K, V]: UIO[MemCache[K, V]] =
    for ref <- Ref.make(Map.empty[K, V])
    yield MemCache(ref)
}
