package controllers

import play.api.mvc._, Results._

import lila.app._
import views._

object Donation extends LilaController {

  def index = Open { implicit ctx =>
    OptionFuOk(Prismic.oneShotBookmark("donate")) {
      case (doc, resolver) => Env.donation.api.list(100) zip
        Env.donation.api.top(5) zip
        Env.donation.api.progress map {
          case ((donations, top), progress) =>
            views.html.donation.index(doc, resolver, donations, top, progress)
        }
    }
  }

  def thanks = Open { implicit ctx =>
    OptionOk(Prismic.oneShotBookmark("donate-thanks")) {
      case (doc, resolver) => views.html.site.page(doc, resolver)
    }
  }

  def ipn = Action.async { implicit req =>
    Env.donation.forms.ipn.bindFromRequest.fold(
      err => {
        println(err)
        fuccess(Ok)
      },
      ipn => {
        val donation = lila.donation.Donation.make(
          payPalTnx = ipn.txnId,
          payPalSub = ipn.subId,
          userId = ipn.userId,
          email = ipn.email,
          name = ipn.name,
          gross = ipn.grossCents,
          fee = ipn.feeCents,
          message = "")
        println(donation)
        Env.donation.api create donation inject Ok
      })
  }
}
