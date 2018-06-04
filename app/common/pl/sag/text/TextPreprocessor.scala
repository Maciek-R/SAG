package pl.sag.text

import java.text.Normalizer

import epic.preprocess.MLSentenceSegmenter


case class StopWordFilter(stopwords: List[String]) {
  def apply(s: String): Boolean = !stopwords.contains(s.toLowerCase)
}

case class WordLengthFilter(minLength: Int) {
  def apply(s: String): Boolean = s.length >= minLength
}

case class NationalCharacterMapper() {
  def apply(s: String): String = {
    Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
      .replaceAll("ł", "l")
  }
}

case class OnlyAlphabeticMapper() {
  def apply(s: String): String = s.replaceAll("[\\W]|_|\\d", "")
}

object TextPreprocessor {

  val stopwords_raw = "a\naby\nach\nacz\naczkolwiek\naj\nalbo\nale\nalez\należ\nani\naz\naż\nbardziej\nbardzo\nbeda\nbedzie\nbez\ndeda\nbędą\nbede\nbędę\nbędzie\nbo\nbowiem\nby\nbyc\nbyć\nbyl\nbyla\nbyli\nbylo\nbyly\nbył\nbyła\nbyło\nbyły\nbynajmniej\ncala\ncali\ncaly\ncała\ncały\nci\ncie\nciebie\ncię\nco\ncokolwiek\ncos\ncoś\nczasami\nczasem\nczemu\nczy\nczyli\ndaleko\ndla\ndlaczego\ndlatego\ndo\ndobrze\ndokad\ndokąd\ndosc\ndość\nduzo\ndużo\ndwa\ndwaj\ndwie\ndwoje\ndzis\ndzisiaj\ndziś\ngdy\ngdyby\ngdyz\ngdyż\ngdzie\ngdziekolwiek\ngdzies\ngdzieś\ngo\ni\nich\nile\nim\ninna\ninne\ninny\ninnych\niz\niż\nja\njak\njakas\njakaś\njakby\njaki\njakichs\njakichś\njakie\njakis\njakiś\njakiz\njakiż\njakkolwiek\njako\njakos\njakoś\nją\nje\njeden\njedna\njednak\njednakze\njednakże\njedno\njego\njej\njemu\njesli\njest\njestem\njeszcze\njeśli\njezeli\njeżeli\njuz\njuż\nkazdy\nkażdy\nkiedy\nkilka\nkims\nkimś\nkto\nktokolwiek\nktora\nktore\nktorego\nktorej\nktory\nktorych\nktorym\nktorzy\nktos\nktoś\nktóra\nktóre\nktórego\nktórej\nktóry\nktórych\nktórym\nktórzy\nku\nlat\nlecz\nlub\nma\nmają\nmało\nmam\nmi\nmiedzy\nmiędzy\nmimo\nmna\nmną\nmnie\nmoga\nmogą\nmoi\nmoim\nmoj\nmoja\nmoje\nmoze\nmozliwe\nmozna\nmoże\nmożliwe\nmożna\nmój\nmu\nmusi\nmy\nna\nnad\nnam\nnami\nnas\nnasi\nnasz\nnasza\nnasze\nnaszego\nnaszych\nnatomiast\nnatychmiast\nnawet\nnia\nnią\nnic\nnich\nnie\nniech\nniego\nniej\nniemu\nnigdy\nnim\nnimi\nniz\nniż\nno\no\nobok\nod\nokoło\non\nona\none\noni\nono\noraz\noto\nowszem\npan\npana\npani\npo\npod\npodczas\npomimo\nponad\nponiewaz\nponieważ\npowinien\npowinna\npowinni\npowinno\npoza\nprawie\nprzeciez\nprzecież\nprzed\nprzede\nprzedtem\nprzez\nprzy\nroku\nrowniez\nrównież\nsam\nsama\nsą\nsie\nsię\nskad\nskąd\nsoba\nsobą\nsobie\nsposob\nsposób\nswoje\nta\ntak\ntaka\ntaki\ntakie\ntakze\ntakże\ntam\nte\ntego\ntej\nten\nteraz\nteż\nto\ntoba\ntobą\ntobie\ntotez\ntoteż\ntotobą\ntrzeba\ntu\ntutaj\ntwoi\ntwoim\ntwoj\ntwoja\ntwoje\ntwój\ntwym\nty\ntych\ntylko\ntym\nu\nw\nwam\nwami\nwas\nwasz\nwasza\nwasze\nwe\nwedług\nwiele\nwielu\nwięc\nwięcej\nwlasnie\nwłaśnie\nwszyscy\nwszystkich\nwszystkie\nwszystkim\nwszystko\nwtedy\nwy\nz\nza\nzaden\nzadna\nzadne\nzadnych\nzapewne\nzawsze\nze\nzeby\nzeznowu\nzł\nznow\nznowu\nznów\nzostal\nzostał\nżaden\nżadna\nżadne\nżadnych\nże\nżeby"

  val sentenceSplitter: MLSentenceSegmenter = MLSentenceSegmenter.bundled().get
  val tokenizer = new epic.preprocess.TreebankTokenizer()

  val wordLengthFilter = WordLengthFilter(2)
  val nationalCharacterMapper = new NationalCharacterMapper
  val stopWordsFilter = StopWordFilter(stopwords_raw.split("\n").map(nationalCharacterMapper(_)).toList)
  val onlyAlphabeticMapper = new OnlyAlphabeticMapper

  def preprocess(document: String): List[String] = {
    val words: IndexedSeq[String] = sentenceSplitter(document).map(tokenizer).toIndexedSeq.flatten

    words
      .map(_.toLowerCase)
      .map(nationalCharacterMapper(_))
      .map(onlyAlphabeticMapper(_))
      .filter(stopWordsFilter(_))
      .filter(wordLengthFilter(_))
      .toList
  }
}
