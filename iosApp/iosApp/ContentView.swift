import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {

    let componentContext: ComponentContext

    let backDispatcher: BackDispatcher

    let deepLinkReceiver: DeepLinkReceiver

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            componentContext: componentContext,
            backDispatcher: backDispatcher,
            deepLinkReceiver: deepLinkReceiver,
            adsConfiguration: UiAdsConfiguration(
                tasksOverviewBannerId: adUnitId(
                    key: "YandexTasksBannerId",
                    demo: "demo-banner-yandex"
                ),
                infoOrganizationsBannerId: adUnitId(
                    key: "YandexInfoBannerId",
                    demo: "demo-banner-yandex"
                ),
                aiQuotaRewardedId: adUnitId(
                    key: "YandexAiRewardedId",
                    demo: "demo-rewarded-yandex"
                ),
                scheduleImportRewardedId: adUnitId(
                    key: "YandexScheduleRewardedId",
                    demo: "demo-rewarded-yandex"
                )
            )
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}

    private func adUnitId(key: String, demo: String) -> String {
        let configured = Bundle.main.object(forInfoDictionaryKey: key) as? String ?? ""
#if DEBUG
        return configured.isEmpty ? demo : configured
#else
        return configured
#endif
    }
}

struct ContentView: View {

    let componentContext: ComponentContext

    let backDispatcher: BackDispatcher

    let deepLinkReceiver: DeepLinkReceiver

    var body: some View {
        ComposeView(
            componentContext: componentContext,
            backDispatcher: backDispatcher,
            deepLinkReceiver: deepLinkReceiver
        ).ignoresSafeArea(.keyboard)
            .ignoresSafeArea(edges: .all)
    }
}
