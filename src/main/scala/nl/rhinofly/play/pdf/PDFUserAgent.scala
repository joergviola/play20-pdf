package nl.rhinofly.play.pdf

import com.lowagie.text.Image
import java.io.{ ByteArrayOutputStream, InputStream, OutputStream }
import org.xhtmlrenderer.pdf.{ ITextFSImage, ITextOutputDevice, ITextUserAgent }
import org.xhtmlrenderer.resource.{ CSSResource, ImageResource, XMLResource }
import play.api.Play

class PDFUserAgent(outputDevice: ITextOutputDevice) extends ITextUserAgent(outputDevice) {

  override def getImageResource(url: String): ImageResource = {
    Play.current.resourceAsStream(url).map { stream =>
      val image = Image.getInstance(getData(stream))
      scaleToOutputResolution(image);
      return new ImageResource(url, new ITextFSImage(image));
    } getOrElse super.getImageResource(url)
  }

  override def getCSSResource(url: String): CSSResource =
    Play.current.resourceAsStream(url)
      .map(new CSSResource(_))
      .getOrElse(super.getCSSResource(url))

  override def getBinaryResource(url: String): Array[Byte] =
    Play.current.resourceAsStream(url)
      .map(getData(_))
      .getOrElse(super.getBinaryResource(url))

  override def getXMLResource(url: String): XMLResource =
    Play.current.resourceAsStream(url)
      .map(XMLResource.load(_))
      .getOrElse(super.getXMLResource(url))

  private def scaleToOutputResolution(image: Image) {
    val factor = getSharedContext().getDotsPerPixel();
    image.scaleAbsolute(image.getPlainWidth() * factor,
      image.getPlainHeight() * factor);
  }

  private def getData(stream: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    copy(stream, baos)
    return baos.toByteArray
  }

  private def copy(input: InputStream, output: OutputStream) = {
    val buffer = new Array[Byte](1024)
    Stream.continually(input.read(buffer)).takeWhile(_ >= buffer.length).foreach(output.write(buffer, 0, _))
  }

}