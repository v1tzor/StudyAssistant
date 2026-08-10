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
            deepLinkReceiver: deepLinkReceiver
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
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
