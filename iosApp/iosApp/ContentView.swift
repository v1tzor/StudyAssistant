import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    
    let componentContext: ComponentContext
    
    let backDispatcher: BackDispatcher
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            componentContext: componentContext,
            backDispatcher: backDispatcher
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    
    let componentContext: ComponentContext
    
    let backDispatcher: BackDispatcher
    
    var body: some View {
        ComposeView(
            componentContext: componentContext,
            backDispatcher: backDispatcher
        ).ignoresSafeArea(.keyboard)
            .ignoresSafeArea(edges: .all)
    }
}
