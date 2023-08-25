package copy

import cats.effect.{IO, Resource}
import cats.syntax.all._
import java.io._

object Copy {
  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStreams(origin, destination).use {
      case (in, out) =>
        transfer(in, out)
    }

  private def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0)

  private def transmit(origin: InputStream, destination: OutputStream, buffer: Array[Byte], acc: Long): IO[Long] =
    for {
      amount  <-  IO.blocking(origin.read(buffer, 0, buffer.length))
      count   <-  if (amount > -1) IO.blocking(destination.write(buffer, 0, amount)) >>
                                  transmit(origin, destination, buffer, acc + amount)
                  else IO.pure(acc)
    } yield count

  private def inputOutputStreams(in: File, out: File): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)

  private def inputStream(f: File): Resource[IO, FileInputStream] =
    Resource.make{
      IO.blocking(new FileInputStream(f))
    } { inStream =>
      IO.blocking(inStream.close()).handleErrorWith(_ => IO.unit)
    }

  private def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.make{
      IO.blocking((new FileOutputStream(f)))
    } { outStream =>
      IO.blocking(outStream.close()).handleErrorWith(_ => IO.unit)
    }

}
