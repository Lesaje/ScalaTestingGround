import cats.effect._
import java.io.File
import copy.Copy

object Main extends IOApp {
  override def run (args: List[String]): IO[ExitCode] =
    for {
      _ <- if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need origin and destination files"))
      else IO.unit
      origin = new File(args(0))
      target = new File(args(1))
      count <- Copy.copy(origin, target)
      _ <- IO.println(s"$count bytes copied from ${origin.getPath} to ${target.getPath}")
    } yield ExitCode.Success
}
