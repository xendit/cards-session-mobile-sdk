//
//  CustomTextFieldStyle.swift
//  Pods
//
//  Created by Ahmad Alfhajri on 11/12/2024.
//

import SwiftUI

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    .background(Color.white)
            )
            .font(.system(size: 16))
    }
}
